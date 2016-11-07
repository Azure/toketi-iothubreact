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

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class AllMessagesAreDelivered extends FeatureSpec with GivenWhenThen {

  info("As a client of Azure IoT hub")
  info("I want to be able to receive all device messages")
  info("So I can process them all")

  // A label shared by all the messages, to filter out data sent by other tests
  val testRunId: String = "[AllMessagesAreDelivered-" + java.util.UUID.randomUUID().toString + "]"

  val counter = actorSystem.actorOf(Props[Counter], "Counter")
  counter ! "reset"

  def readCounter: Long = {
    Await.result(counter.ask("get")(5 seconds), 5 seconds).asInstanceOf[Long]
  }

  feature("All IoT messages are presented as an ordered stream") {

    scenario("Application wants to retrieve all IoT messages") {

      // How many seconds we allow the test to wait for messages from the stream
      val TestTimeout = 60 seconds
      val DevicesCount = 5
      val MessagesPerDevice = 4
      val expectedMessageCount = DevicesCount * MessagesPerDevice

      // We'll use this as the streaming start date
      val startTime = Instant.now().minusSeconds(30)
      log.info(s"Test run: ${testRunId}, Start time: ${startTime}")

      Given("An IoT hub is configured")
      val messages = IoTHub().source(startTime, false)

      And(s"${DevicesCount} devices have sent ${MessagesPerDevice} messages each")
      for (i ← 0 until DevicesCount) {
        val device = new Device("device" + (10000 + i))
        for (i ← 1 to MessagesPerDevice) device.sendMessage(testRunId, i)
        device.disconnect()
      }
      log.info(s"Messages sent: $expectedMessageCount")

      When("A client application processes messages from the stream")
      counter ! "reset"
      val count = Sink.foreach[MessageFromDevice] {
        m ⇒ counter ! "inc"
      }

      val (killSwitch, last) = messages
        .viaMat(KillSwitches.single)(Keep.right)
        .filter(m ⇒ m.contentAsString contains testRunId)
        .toMat(count)(Keep.both)
        .run()

      Then("Then the client application receives all the messages sent")
      var time = TestTimeout.toMillis.toInt
      val pause = time / 10
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
