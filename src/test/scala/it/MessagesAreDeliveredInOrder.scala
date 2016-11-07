// Copyright (c) Microsoft. All rights reserved.

package it

import java.time.Instant

import akka.actor.Props
import akka.pattern.ask
import akka.stream.KillSwitches
import akka.stream.scaladsl.{Keep, Sink}
import com.microsoft.azure.iot.iothubreact.MessageFromDevice
import com.microsoft.azure.iot.iothubreact.ResumeOnError._
import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHub
import it.helpers.{Counter, Device}
import org.scalatest._

import scala.collection.parallel.mutable
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class MessagesAreDeliveredInOrder extends FeatureSpec with GivenWhenThen {

  info("As a client of Azure IoT hub")
  info("I want to receive the messages in order")
  info("So I can process them in order")

  // A label shared by all the messages, to filter out data sent by other tests
  val testRunId: String = "[MessagesAreDeliveredInOrder-" + java.util.UUID.randomUUID().toString + "]"

  val counter = actorSystem.actorOf(Props[Counter], "Counter")
  counter ! "reset"

  def readCounter: Long = {
    Await.result(counter.ask("get")(5 seconds), 5 seconds).asInstanceOf[Long]
  }

  feature("All IoT messages are presented as an ordered stream") {

    // Note: messages are sent in parallel to obtain some level of mix in the
    // storage, so do not refactor, i.e. don't do one device at a time.
    scenario("Customer needs to process IoT messages in the right order") {

      // How many seconds we allow the test to wait for messages from the stream
      val TestTimeout = 120 seconds
      val DevicesCount = 10
      val MessagesPerDevice = 200
      val expectedMessageCount = DevicesCount * MessagesPerDevice

      // We'll use this as the streaming start date
      val startTime = Instant.now().minusSeconds(30)
      log.info(s"Test run: ${testRunId}, Start time: ${startTime}")

      Given("An IoT hub is configured")
      val messages = IoTHub().source(startTime, false)

      And(s"${DevicesCount} devices have sent ${MessagesPerDevice} messages each")
      val devices = new collection.mutable.ListMap[Int, Device]()

      for (i ← 0 until DevicesCount)
        devices(i) = new Device("device" + (10000 + i))

      for (i ← 1 to MessagesPerDevice)
        for (i ← 0 until DevicesCount)
          devices(i).sendMessage(testRunId, i)

      for (i ← 0 until DevicesCount)
        devices(i).disconnect()

      log.info(s"Messages sent: $expectedMessageCount")

      When("A client application processes messages from the stream")

      Then("Then the client receives all the messages ordered within each device")
      counter ! "reset"
      val cursors = new mutable.ParHashMap[String, Long]
      val verifier = Sink.foreach[MessageFromDevice] {
        m ⇒ {
          counter ! "inc"
          log.debug(s"device: ${m.deviceId}, seq: ${m.sequenceNumber} ")

          if (!cursors.contains(m.deviceId)) {
            cursors.put(m.deviceId, m.sequenceNumber)
          }
          if (cursors(m.deviceId) > m.sequenceNumber) {
            fail(s"Message out of order. " +
              s"Device ${m.deviceId}, message ${m.sequenceNumber} arrived " +
              s"after message ${cursors(m.deviceId)}")
          }
          cursors.put(m.deviceId, m.sequenceNumber)
        }
      }

      val (killSwitch, last) = messages
        .viaMat(KillSwitches.single)(Keep.right)
        .filter(m ⇒ m.contentAsString contains (testRunId))
        .toMat(verifier)(Keep.both)
        .run()

      // Wait till all messages have been verified
      var time = TestTimeout.toMillis.toInt
      val pause = time / 12
      var actualMessageCount = readCounter
      while (time > 0 && actualMessageCount < expectedMessageCount) {
        Thread.sleep(pause)
        time -= pause
        actualMessageCount = readCounter
        log.info(s"Messages received so far: ${actualMessageCount} of ${expectedMessageCount} [Time left ${time / 1000} secs]")
      }

      killSwitch.shutdown()

      assert(
        actualMessageCount == expectedMessageCount,
        s"Expecting ${expectedMessageCount} messages but received ${actualMessageCount}")
    }
  }
}
