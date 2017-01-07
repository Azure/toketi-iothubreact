// Copyright (c) Microsoft. All rights reserved.

package OSN.Demo.Checkpoints

import akka.stream.scaladsl.Sink
import com.microsoft.azure.iot.iothubreact.MessageFromDevice
import com.microsoft.azure.iot.iothubreact.filters.{Device, MessageType}
import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHub
import com.microsoft.azure.iot.iothubreact.ResumeOnError._

object Console {

  def apply() = Sink.foreach[MessageFromDevice] {

    m ⇒ println(
      s"${m.created} - ${m.deviceId} - ${m.messageType}"
        + s" - ${m.contentAsString}")

  }
}

object Demo extends App {

  IoTHub()

    .source(withCheckpoints = true) // <===

    .filter(MessageType("temperature"))

    .filter(Device("device1000"))

    .to(Console())

    .run()
}
