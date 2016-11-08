// Copyright (c) Microsoft. All rights reserved.

import java.time.Instant

import com.microsoft.azure.eventhubs.EventHubClient
import com.microsoft.azure.servicebus.ConnectionStringBuilder
import messagesFromDevices.MessageFromDevice

import scala.collection.JavaConverters._
import scala.language.{implicitConversions, postfixOps}

/**
  * Receive messages sent from IoT devices, from one EventHub partition
  */
object Main extends App {

  val partition = 0
  val startTime = Instant.now.minusSeconds(3600)

  val attemptsOnEmpty = 3
  val sleepOnEmpty    = 2000

  val connString = new ConnectionStringBuilder(
    Configuration.iotHubNamespace,
    Configuration.iotHubName,
    Configuration.iotHubKeyName,
    Configuration.iotHubKey).toString
  println(s"Connecting to ${Configuration.iotHubName} ${Configuration.iotHubNamespace} ${Configuration.iotHubKeyName}")

  val client = EventHubClient.createFromConnectionStringSync(connString)
  println("Client ready")

  val receiver = client.createReceiverSync(Configuration.receiverConsumerGroup, partition.toString, startTime)
  println(s"Receiver ready, partition ${partition}, start ${startTime}}")

  println("Downloading messages")

  var attempts = attemptsOnEmpty
  var continue = true
  while (continue) {
    val messages = receiver.receiveSync(Configuration.receiverBatchSize)

    val iterator = messages.asScala.map(e ⇒ MessageFromDevice(e, Some(partition))).toList

    if (iterator.size == 0) {
      println("No messages")
      Thread.sleep(sleepOnEmpty)
      attempts -= 1
      continue = attempts > 0
    } else {
      attempts = attemptsOnEmpty
      iterator.foreach(t ⇒ {
        println(s"${t.offset} - ${t.model} - ${t.created}")
      })
    }
  }

  println("Done")
}
