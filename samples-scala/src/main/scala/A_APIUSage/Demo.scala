// Copyright (c) Microsoft. All rights reserved.

package A_APIUSage

import java.time.Instant

import akka.stream.scaladsl.Sink
import com.microsoft.azure.iot.iothubreact.ResumeOnError._
import com.microsoft.azure.iot.iothubreact.filters._
import com.microsoft.azure.iot.iothubreact.scaladsl._
import com.microsoft.azure.iot.iothubreact.{MessageFromDevice, MessageToDevice}
import com.microsoft.azure.sdk.iot.service.DeliveryAcknowledgement

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}

/** Stream all messages from beginning
  */
object AllMessagesFromBeginning extends App {

  println("Streaming all the messages")

  val messages = IoTHub().source()

  val console = Sink.foreach[MessageFromDevice] {
    m ⇒ println(s"${m.created} - ${m.deviceId} - ${m.messageType} - ${m.contentAsString}")
  }

  messages

    .to(console)

    .run()
}

/** Stream recent messages
  */
object OnlyRecentMessages extends App {

  println("Streaming recent messages")

  val messages = IoTHub().source(java.time.Instant.now())

  val console = Sink.foreach[MessageFromDevice] {
    m ⇒ println(s"${m.created} - ${m.deviceId} - ${m.messageType} - ${m.contentAsString}")
  }

  messages

    .to(console)

    .run()
}

/** Stream only from partitions 0 and 3
  */
object OnlyTwoPartitions extends App {

  val Partition1 = 0
  val Partition2 = 3

  println(s"Streaming messages from partitions ${Partition1} and ${Partition2}")

  val messages = IoTHub().source(PartitionList(Seq(Partition1, Partition2)))

  val console = Sink.foreach[MessageFromDevice] {
    m ⇒ println(s"${m.created} - ${m.deviceId} - ${m.messageType} - ${m.contentAsString}")
  }

  messages

    .to(console)

    .run()
}

/** Stream to 2 different consoles
  */
object MultipleDestinations extends App {

  println("Streaming to two different consoles")

  val messages = IoTHub().source(java.time.Instant.now())

  val console1 = Sink.foreach[MessageFromDevice] {
    m ⇒ if (m.messageType == "temperature") println(s"Temperature console: ${m.created} - ${m.deviceId} - ${m.contentAsString}")
  }

  val console2 = Sink.foreach[MessageFromDevice] {
    m ⇒ if (m.messageType == "humidity") println(s"Humidity console: ${m.created} - ${m.deviceId} - ${m.contentAsString}")
  }

  messages

    .alsoTo(console1)

    .to(console2)

    .run()
}

/** Stream only temperature messages
  */
object FilterByMessageType extends App {

  println("Streaming only temperature messages")

  val messages = IoTHub().source()

  val console = Sink.foreach[MessageFromDevice] {
    m ⇒ println(s"${m.created} - ${m.deviceId} - ${m.messageType} - ${m.contentAsString}")
  }

  messages

    .filter(MessageType("temperature")) // Equivalent to: m ⇒ m.messageType == "temperature"

    .to(console)

    .run()
}

/** Stream only messages from "device1000"
  */
object FilterByDeviceID extends App {

  val DeviceID = "device1000"

  println(s"Streaming only messages from ${DeviceID}")

  val messages = IoTHub().source()

  val console = Sink.foreach[MessageFromDevice] {
    m ⇒ println(s"${m.created} - ${m.deviceId} - ${m.messageType} - ${m.contentAsString}")
  }

  messages
    .filter(Device(DeviceID)) // Equivalent to: m ⇒ m.deviceId == DeviceID

    .to(console)

    .run()
}

/** Show how to close the stream, terminating the connections to Azure IoT hub
  */
object CloseStream extends App {

  println("Streaming all the messages, will stop in 5 seconds")

  implicit val system = akka.actor.ActorSystem("system")

  system.scheduler.scheduleOnce(5 seconds) {
    hub.close()
  }

  val hub      = IoTHub()
  val messages = hub.source()

  var console = Sink.foreach[MessageFromDevice] {
    m ⇒ println(s"${m.created} - ${m.deviceId} - ${m.messageType} - ${m.contentAsString}")
  }

  messages.to(console).run()
}

/** Send a message to a device
  */
object SendMessageToDevice extends App with Deserialize {

  val message = MessageToDevice("Turn fan ON")
    .addProperty("speed", "high")
    .addProperty("duration", "60")
    .expiry(Instant.now().plusSeconds(30))
    .ack(DeliveryAcknowledgement.Full)

  val hub = IoTHub()

  hub
    .source(java.time.Instant.now())
    .filter(MessageType("temperature"))
    .map(deserialize)
    .filter(_.value > 15)
    .map(t ⇒ message.to(t.deviceId))
    .to(hub.sink())
    .run()
}
