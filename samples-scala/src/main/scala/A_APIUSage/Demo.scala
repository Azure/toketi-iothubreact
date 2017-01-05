// Copyright (c) Microsoft. All rights reserved.
package A_APIUSage

import akka.stream.scaladsl.Sink
import com.microsoft.azure.iot.iothubreact.MessageFromDevice
import com.microsoft.azure.iot.iothubreact.ResumeOnError._
import com.microsoft.azure.iot.iothubreact.filters._
import com.microsoft.azure.iot.iothubreact.scaladsl._

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

/** Stream only from partition 0
  */
object OnlyOnePartition extends App {

  val Partition = 0

  println(s"Streaming messages from partition ${Partition}")

  val messages = IoTHubPartition(Partition).source()

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
