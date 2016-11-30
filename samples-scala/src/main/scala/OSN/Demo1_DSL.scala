// Copyright (c) Microsoft. All rights reserved.

package OSN.Demo.Simple

import akka.stream.scaladsl.Sink
import com.microsoft.azure.iot.iothubreact.MessageFromDevice
import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHub
import com.microsoft.azure.iot.iothubreact.ResumeOnError._

object Console {

  def apply() = Sink.foreach[MessageFromDevice] {

    m ⇒ println(
      s"${m.created} - ${m.deviceId} - ${m.model}"
        + s" - ${m.contentAsString}")

  }
}

object Demo extends App {

  IoTHub()

    .source()

    .to(Console())

    .run()
}
