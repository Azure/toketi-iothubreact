// Copyright (c) Microsoft. All rights reserved.

package OSN.Demo.Checkpoints

import akka.stream.scaladsl.Sink
import com.microsoft.azure.iot.iothubreact.{MessageFromDevice, SourceOptions}
import com.microsoft.azure.iot.iothubreact.filters.{Device, MessageSchema}
import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHub
import com.microsoft.azure.iot.iothubreact.ResumeOnError._

object Console {

  def apply() = Sink.foreach[MessageFromDevice] {

    m ⇒ println(
      s"${m.received} - ${m.deviceId} - ${m.messageSchema}"
        + s" - ${m.contentAsString}")
  }
}

object Demo extends App {

  IoTHub()

    .source(SourceOptions().savePosition()) // <===

    .filter(MessageSchema("temperature"))

    .filter(Device("device1000"))

    .to(Console())

    .run()
}
