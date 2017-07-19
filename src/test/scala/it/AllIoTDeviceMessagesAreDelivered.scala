// Copyright (c) Microsoft. All rights reserved.

package it

import java.time.Instant

import akka.actor.Props
import akka.pattern.ask
import akka.stream.scaladsl.Sink
import com.microsoft.azure.iot.iothubreact.{MessageFromDevice, SourceOptions}
import com.microsoft.azure.iot.iothubreact.ResumeOnError._
import com.microsoft.azure.iot.iothubreact.checkpointing.backends.CheckpointBackend
import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHub
import it.helpers.{Counter, Device}
import org.scalatest._

import scala.collection.concurrent.TrieMap
import scala.concurrent.{Await, Future}
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

    var commits = TrieMap[Int, Seq[String]]()
    class CustomBackend extends CheckpointBackend {

      override def readOffset(partition: Int): String = {
        return commits.getOrElse(partition, Seq()).lastOption.getOrElse("-1")
      }

      override def writeOffset(partition: Int, offset: String): Unit = {
        val row = commits.getOrElse(partition, Seq()) :+ offset
        commits += partition → row
      }
    }

    Feature("All IoT device messages are delivered") {

      Scenario("Application wants to retrieve all IoT messages") {

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
        log.info("Test run: {}, Start time: {}", testRunId, startTime)

        Given("An IoT hub is configured")
        val hub = IoTHub()
        val messages = hub.source(SourceOptions().fromTime(startTime))

        And(s"${DevicesCount} devices have sent ${MessagesPerDevice} messages each")
        for (msgNumber ← 1 to MessagesPerDevice) {
          for (deviceNumber ← 0 until DevicesCount) {
            devices(deviceNumber).sendMessage(testRunId, msgNumber)
            // Workaround for https://github.com/Azure/azure-iot-sdk-java/issues/19 (DeviceClient creates a "cert.crt" file)
            //if (msgNumber == 1) devices(deviceNumber).waitConfirmation()
          }
          for (deviceNumber ← 0 until DevicesCount) devices(deviceNumber).waitConfirmation()
        }

        for (deviceNumber ← 0 until DevicesCount) devices(deviceNumber).disconnect()

        log.info("Messages sent: {}", expectedMessageCount)

        When("A client application processes messages from the stream")
        counter ! "reset"

        //send to offset sink
        implicit val backend: CustomBackend = new CustomBackend()
        val os = hub.offsetSaveSink()
        var maxOffset = TrieMap[Int, Long]()
        messages
          .filter(m ⇒ m.contentAsString contains testRunId)
          .map {
            m ⇒
              counter ! "inc"
              m.runtimeInfo.partitionInfo.partitionNumber.map {
                p ⇒
                  maxOffset += p → math.max(m.offset.toLong, maxOffset.getOrElse(p, -1L))
              }
              m
          }
          .mapAsync(10) {
            m ⇒
              //slow down higher offsets and process asynchronously so that offsets are likely to be out of order
              val offset = m.offset.toLong
              Thread.sleep(math.log10(offset).toInt)
              Future successful m
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
          log.info("Messages received so far: {} of {} [Time left {} secs]", actualMessageCount, expectedMessageCount, time / 1000)
        }

        hub.close()

        assert(actualMessageCount == expectedMessageCount,
          s"Expecting ${expectedMessageCount} messages but received ${actualMessageCount}")

        Then("Then the offsets should be saved in ascending order")
        assert(commits.size >= 1, "Commits should have at least one partition to them")
        assert(commits.head._2.size >= 1, "Commits should have at least one commit")

        commits.map {
          case (partition, offsets) ⇒
            assert(offsets.last.toLong == maxOffset(partition), s"Partition ${partition} should have last stored the max offset (${offsets.last} vs. ${maxOffset(partition)})")
        }

      }
    }
  }
}
