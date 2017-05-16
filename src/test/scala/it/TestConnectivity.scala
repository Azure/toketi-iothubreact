// Copyright (c) Microsoft. All rights reserved.

package it

import java.time.Instant

import com.microsoft.azure.eventhubs.{EventHubClient, PartitionReceiver}
import com.microsoft.azure.iot.iothubreact.ResumeOnError._
import com.microsoft.azure.servicebus.ConnectionStringBuilder
import it.helpers.{Configuration, Device}
import org.scalatest._

import scala.collection.JavaConverters._
import scala.language.{implicitConversions, postfixOps}

class TestConnectivity extends FeatureSpec with GivenWhenThen {

  // TODO: we should use tags
  if (!sys.env.contains("TRAVIS_PULL_REQUEST") || sys.env("TRAVIS_PULL_REQUEST") == "false") {

    info("As a test runner")
    info("I want to connect to EventuHub")
    info("So I can run the test suite")

    // A label shared by all the messages, to filter out data sent by other tests
    val testRunId = s"[${this.getClass.getName}-" + java.util.UUID.randomUUID().toString + "]"
    val startTime = Instant.now().minusSeconds(60)

    Feature("The test suite can connect to IoT Hub") {

      Scenario("The test uses the configured credentials") {

        // Enough devices to hit the first partitions, so that the test ends quickly
        val DevicesCount = 10

        // Create devices
        val devices = new collection.mutable.ListMap[Int, Device]()
        for (deviceNumber ← 0 until DevicesCount) devices(deviceNumber) = new Device("device" + (10000 + deviceNumber))

        // Send a message from each device
        for (deviceNumber ← 0 until DevicesCount) {
          devices(deviceNumber).sendMessage(testRunId, 0)
          // Workaround for issue 995
          devices(deviceNumber).waitConfirmation()
        }

        // Wait and disconnect
        for (deviceNumber ← 0 until DevicesCount) {
          devices(deviceNumber).waitConfirmation()
          devices(deviceNumber).disconnect()
        }

        val connString = new ConnectionStringBuilder(
          Configuration.iotHubNamespace,
          Configuration.iotHubName,
          Configuration.accessPolicy,
          Configuration.accessKey).toString

        log.info("Connecting to IoT Hub")
        val client = EventHubClient.createFromConnectionStringSync(connString)

        var found = false
        var attempts = 0
        var p = 0

        // Check that at least one message arrived to IoT Hub
        while (!found && p < Configuration.iotHubPartitions) {

          log.info("Checking partition {}", p)
          val receiver: PartitionReceiver = client.createReceiverSync(Configuration.receiverConsumerGroup, p.toString, startTime)

          log.info("Receiver getEpoch(): " + receiver.getEpoch)
          log.info("Receiver getPartitionId(): " + receiver.getPartitionId)
          log.info("Receiver getPrefetchCount(): " + receiver.getPrefetchCount)
          log.info("Receiver getReceiveTimeout(): " + receiver.getReceiveTimeout)

          attempts = 0
          while (!found && attempts < 100) {
            attempts += 1

            log.info("Receiving batch {}", attempts)
            val records = receiver.receiveSync(999)
            if (records == null) {
              attempts = Int.MaxValue
              log.info("This partition is empty")
            } else {
              val messages = records.asScala
              log.info("Messages retrieved {}", messages.size)

              val matching = messages.filter(e ⇒ new String(e.getBytes) contains testRunId)
              log.info("Matching messages {}", matching.size)

              found = (matching.size > 0)
            }
          }

          p += 1
        }

        assert(found, "Expecting to find at least one of the messages sent")
      }
    }
  }
}
