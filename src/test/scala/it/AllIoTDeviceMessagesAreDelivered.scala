// Copyright (c) Microsoft. All rights reserved.

package it

import java.time.Instant

import akka.actor.Props
import akka.pattern.ask
import akka.stream.scaladsl.Sink
import com.microsoft.azure.iot.iothubreact.MessageFromDevice
import com.microsoft.azure.iot.iothubreact.ResumeOnError._
import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHub
import it.helpers.{Counter, Device}
import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class AllIoTDeviceMessagesAreDelivered extends FeatureSpec with GivenWhenThen {

  // TODO: we should use tags
  if (!sys.env.contains("TRAVIS_PULL_REQUEST") || sys.env("TRAVIS_PULL_REQUEST") == "false") {

    info("As a client of Azure IoT hub")
    info("I want to be able to receive all device messages")
    info("So I can process them all")

    // A label shared by all the messages, to filter out data sent by other tests
    val testRunId: String = s"[${this.getClass.getName}-" + java.util.UUID.randomUUID().toString + "]"

    val counter = actorSystem.actorOf(Props[Counter], this.getClass.getName + "Counter")
    counter ! "reset"

    def readCounter: Long = {
      Await.result(counter.ask("get")(5 seconds), 5 seconds).asInstanceOf[Long]
    }

    feature("All IoT device messages are delivered") {

      scenario("Application wants to retrieve all IoT messages") {

        // How many seconds we allow the test to wait for messages from the stream
        val TestTimeout = 60 seconds
        val DevicesCount = 5
        val MessagesPerDevice = 3
        val expectedMessageCount = DevicesCount * MessagesPerDevice

        // Create devices
        val devices = new collection.mutable.ListMap[Int, Device]()
        for (deviceNumber ← 0 until DevicesCount) devices(deviceNumber) = new Device("device" + (10000 + deviceNumber))

        // We'll use this as the streaming start date
        val startTime = Instant.now().minusSeconds(30)
        log.info(s"Test run: ${testRunId}, Start time: ${startTime}")

        Given("An IoT hub is configured")
        val hub = IoTHub()
        val messages = hub.source(startTime, false)

        And(s"${DevicesCount} devices have sent ${MessagesPerDevice} messages each")
        for (msgNumber ← 1 to MessagesPerDevice) {
          for (deviceNumber ← 0 until DevicesCount) {
            devices(deviceNumber).sendMessage(testRunId, msgNumber)
            // Workaround for issue 995
            if (msgNumber == 1) devices(deviceNumber).waitConfirmation()
          }
          for (deviceNumber ← 0 until DevicesCount) devices(deviceNumber).waitConfirmation()
        }

        for (deviceNumber ← 0 until DevicesCount) devices(deviceNumber).disconnect()

        log.info(s"Messages sent: $expectedMessageCount")

        When("A client application processes messages from the stream")
        counter ! "reset"

        //send to offset sink
        val os = hub.offsetSink(1)
        messages
          .filter(m ⇒ m.contentAsString contains testRunId)
          .map{ m ⇒
            counter ! "inc"
            m
          }
          .runWith(os)

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

        hub.close()

        assert(actualMessageCount == expectedMessageCount,
          s"Expecting ${expectedMessageCount} messages but received ${actualMessageCount}")
      }
    }
  }

}
