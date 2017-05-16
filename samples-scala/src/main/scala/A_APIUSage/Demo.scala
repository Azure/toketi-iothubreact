// Copyright (c) Microsoft. All rights reserved.

package A_APIUSage

import java.time.Instant

import akka.Done
import akka.stream.scaladsl.{Flow, Sink}
import com.microsoft.azure.iot.iothubreact.ResumeOnError._
import com.microsoft.azure.iot.iothubreact.filters._
import com.microsoft.azure.iot.iothubreact.scaladsl._
import com.microsoft.azure.iot.iothubreact.{MessageFromDevice, MessageToDevice, SourceOptions}
import com.microsoft.azure.sdk.iot.service.DeliveryAcknowledgement

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}

/** Stream all messages from beginning
  */
object AllMessagesFromBeginning extends App {

  println("Streaming all the messages")

  val messages = IoTHub().source()

  val console = Sink.foreach[MessageFromDevice] {
    m ⇒ println(s"${m.received} - ${m.deviceId} - ${m.messageSchema} - ${m.contentAsString}")
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
    m ⇒ println(s"${m.received} - ${m.deviceId} - ${m.messageSchema} - ${m.contentAsString}")
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

  val messages = IoTHub().source(Seq(Partition1, Partition2))

  val console = Sink.foreach[MessageFromDevice] {
    m ⇒ println(s"${m.received} - ${m.deviceId} - ${m.messageSchema} - ${m.contentAsString}")
  }

  messages

    .to(console)

    .run()
}

/** Stream and save the position while streaming
  */
object StoreOffsetsWhileStreaming extends App {

  println(s"Streaming messages and save offsets while streaming")

  val messages = IoTHub().source(SourceOptions().saveOffsets())

  val console = Sink.foreach[MessageFromDevice] {
    m ⇒ println(s"${m.received} - ${m.deviceId} - ${m.messageSchema} - ${m.contentAsString}")
  }

  messages

    .to(console)

    .run()
}

/** Streaming messages from a saved position, without updating the position stored
  */
object StartFromStoredOffsetsButDontWriteNewOffsets extends App {

  println(s"Streaming messages from a saved position, without updating the position stored")

  val messages = IoTHub().source(SourceOptions().fromSavedOffsets())

  val console = Sink.foreach[MessageFromDevice] {
    m ⇒ println(s"${m.received} - ${m.deviceId} - ${m.messageSchema} - ${m.contentAsString}")
  }

  messages

    .to(console)

    .run()
}

/** Streaming messages from a saved position, without updating the position stored
  */
object StartFromStoredOffsetsWithAtLeastOnceSemantics extends App {

  println(s"Streaming messages from a saved position using at least once delivery semantics")

  val hub = IoTHub()
  val messages = hub.source(SourceOptions().fromSavedOffsets())

  val console = Flow[MessageFromDevice].map {
    m ⇒ println(s"${m.received} - ${m.deviceId} - ${m.messageSchema} - ${m.contentAsString}")
    m
  }

  messages
    .via(console)
    .to(hub.offsetSink(32))
    .run()
}

/** Streaming messages from a saved position, without updating the position stored.
  * If there is no position saved, start from one hour in the past.
  */
object StartFromStoredOffsetsIfAvailableOrByTimeOtherwise extends App {

  println(s"Streaming messages from a saved position, without updating the position stored. If there is no position saved, start from one hour in the past.")

  val messages = IoTHub().source(SourceOptions().fromSavedOffsets(Instant.now().minusSeconds(3600)))

  val console = Sink.foreach[MessageFromDevice] {
    m ⇒ println(s"${m.received} - ${m.deviceId} - ${m.messageSchema} - ${m.contentAsString}")
  }

  messages

    .to(console)

    .run()
}

object StreamIncludingRuntimeInformation extends App {

  println(s"Stream messages and print how many messages are left in each partition.")

  val messages = IoTHub().source(SourceOptions().fromStart().withRuntimeInfo())

  val console = Sink.foreach[MessageFromDevice] {
    m ⇒ println(s"Partition ${m.runtimeInfo.partitionInfo.partitionNumber.get}: " +
      s"${m.runtimeInfo.partitionInfo.lastSequenceNumber.get - m.sequenceNumber} messages left to stream")
  }

  messages

    .to(console)

    .run()
}

object MultipleStreamingOptionsAndSyntaxSugar extends App {

  println(s"Streaming messages and save position")

  val options = SourceOptions()
    .partitions(0, 2, 3)
    .fromOffsets("614", "64365", "123512")
    .saveOffsets()

  val messages = IoTHub().source(options)

  val console = Sink.foreach[MessageFromDevice] {
    m ⇒ println(s"${m.received} - ${m.deviceId} - ${m.messageSchema} - ${m.contentAsString}")
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
    m ⇒ if (m.messageSchema == "temperature") println(s"Temperature console: ${m.received} - ${m.deviceId} - ${m.contentAsString}")
  }

  val console2 = Sink.foreach[MessageFromDevice] {
    m ⇒ if (m.messageSchema == "humidity") println(s"Humidity console: ${m.received} - ${m.deviceId} - ${m.contentAsString}")
  }

  messages

    .alsoTo(console1)

    .to(console2)

    .run()
}

/** Stream only temperature messages
  */
object FilterByMessageSchema extends App {

  println("Streaming only temperature messages")

  val messages = IoTHub().source()

  val console = Sink.foreach[MessageFromDevice] {
    m ⇒ println(s"${m.received} - ${m.deviceId} - ${m.messageSchema} - ${m.contentAsString}")
  }

  messages

    .filter(MessageSchema("temperature")) // Equivalent to: m ⇒ m.messageSchema == "temperature"

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
    m ⇒ println(s"${m.received} - ${m.deviceId} - ${m.messageSchema} - ${m.contentAsString}")
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
    m ⇒ println(s"${m.received} - ${m.deviceId} - ${m.messageSchema} - ${m.contentAsString}")
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
    .filter(MessageSchema("temperature"))
    .map(deserialize)
    .filter(_.value > 15)
    .map(t ⇒ message.to(t.deviceId))
    .to(hub.sink())
    .run()
}
