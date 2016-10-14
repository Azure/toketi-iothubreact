// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.test

import java.time.Instant

import akka.NotUsed
import akka.actor.Props
import akka.pattern.ask
import akka.stream.scaladsl.{Sink, Source}
import com.microsoft.azure.iot.iothubreact.IoTMessage
import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHub
import com.microsoft.azure.iot.iothubreact.test.helpers._
import org.scalatest._

import scala.collection.parallel.mutable
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/** Tests streaming against Azure IoT hub endpoint
  *
  * Note: the tests require an actual hub ready to use
  */
class IoTHubReactiveStreamingUserStory
  extends FeatureSpec
    with GivenWhenThen
    with ReactiveStreaming
    with Logger {

  info("As a client of Azure IoT hub")
  info("I want to be able to receive all the messages as a stream")
  info("So I can process them asynchronously and at scale")

  val counter = actorSystem.actorOf(Props[Counter], "Counter")
  counter ! "reset"

  def readCounter: Long = {
    Await.result(counter.ask("get")(5 seconds), 5 seconds).asInstanceOf[Long]
  }

  feature("All IoT messages are presented as an ordered stream") {

    scenario("Developer wants to retrieve IoT messages") {

      Given("An IoT hub is configured")
      val hub = new IoTHub()

      When("A developer wants to fetch messages from Azure IoT hub")
      val messagesFromOnePartition: Source[IoTMessage, NotUsed] = hub.source(1)
      val messagesFromAllPartitions: Source[IoTMessage, NotUsed] = hub.source
      val messagesFromNowOn: Source[IoTMessage, NotUsed] = hub.source(Instant.now())

      Then("The messages are presented as a stream")
      messagesFromOnePartition.to(Sink.ignore)
      messagesFromAllPartitions.to(Sink.ignore)
      messagesFromNowOn.to(Sink.ignore)
    }

    scenario("Application wants to retrieve all IoT messages") {

      // How many seconds we allow the test to wait for messages from the stream
      val TestTimeout = 60 seconds
      val DevicesCount = 5
      val MessagesPerDevice = 4
      val expectedMessageCount = DevicesCount * MessagesPerDevice

      // A label shared by all the messages, to filter out data sent by other tests
      val testRunId: String = "[RetrieveAll-" + java.util.UUID.randomUUID().toString + "]"

      // We'll use this as the streaming start date
      val startTime = Instant.now()
      log.info(s"Test run: ${testRunId}, Start time: ${startTime}")

      Given("An IoT hub is configured")
      val messages = new IoTHub().source(startTime)

      And(s"${DevicesCount} devices have sent ${MessagesPerDevice} messages each")
      for (i ← 0 until DevicesCount) {
        val device = new Device("device" + (10000 + i))
        for (i ← 1 to MessagesPerDevice) device.sendMessage(testRunId, i)
        device.disconnect()
      }
      log.info(s"Messages sent: $expectedMessageCount")

      When("A client application processes messages from the stream")
      counter ! "reset"
      val count = Sink.foreach[IoTMessage] {
        m ⇒ counter ! "inc"
      }

      messages
        .filter(m ⇒ m.contentAsString contains testRunId)
        .to(count)
        .run()

      Then("Then the client application receives all the messages sent")
      var time = TestTimeout.toMillis.toInt
      val pause = time / 10
      var actualMessageCount = readCounter
      while (time > 0 && actualMessageCount < expectedMessageCount) {
        Thread.sleep(pause)
        time -= pause
        actualMessageCount = readCounter
        log.info(s"Messages received so far: ${actualMessageCount} [Time left ${time / 1000} secs]")
      }

      assert(
        actualMessageCount == expectedMessageCount,
        s"Expecting ${expectedMessageCount} messages but received ${actualMessageCount}")
    }

    // Note: messages are sent in parallel to obtain some level of mix in the
    // storage, so do not refactor, i.e. don't do one device at a time.
    scenario("Customer needs to process IoT messages in the right order") {

      // How many seconds we allow the test to wait for messages from the stream
      val TestTimeout = 120 seconds
      val DevicesCount = 10
      val MessagesPerDevice = 200
      val expectedMessageCount = DevicesCount * MessagesPerDevice

      // A label shared by all the messages, to filter out data sent by other tests
      val testRunId: String = "[VerifyOrder-" + java.util.UUID.randomUUID().toString + "]"

      // We'll use this as the streaming start date
      val startTime = Instant.now()
      log.info(s"Test run: ${testRunId}, Start time: ${startTime}")

      Given("An IoT hub is configured")
      val messages = new IoTHub().source(startTime)

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
      val verifier = Sink.foreach[IoTMessage] {
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

      messages
        .filter(m ⇒ m.contentAsString contains (testRunId))
        .to(verifier)
        .run()

      // Wait till all messages have been verified
      var time = TestTimeout.toMillis.toInt
      val pause = time / 10
      var actualMessageCount = readCounter
      while (time > 0 && actualMessageCount < expectedMessageCount) {
        Thread.sleep(pause)
        time -= pause
        actualMessageCount = readCounter
        log.info(s"Messages received so far: ${actualMessageCount} [Time left ${time / 1000} secs]")
      }

      assert(
        actualMessageCount == expectedMessageCount,
        s"Expecting ${expectedMessageCount} messages but received ${actualMessageCount}")
    }
  }
}
