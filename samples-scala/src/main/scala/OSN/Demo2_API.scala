// Copyright (c) Microsoft. All rights reserved.

package OSN.Demo.More

import akka.stream.scaladsl.Sink
import com.microsoft.azure.iot.iothubreact.MessageFromDevice
import com.microsoft.azure.iot.iothubreact.filters.{Device, Model}
import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHub
import com.microsoft.azure.iot.iothubreact.ResumeOnError._

object Console {

  def apply() = Sink.foreach[MessageFromDevice] {

    m ⇒ println(
      s"${m.created} - ${m.deviceId} - ${m.model}"
        + s" - ${m.contentAsString}")

  }
}

object Storage {

  def apply() = Sink.foreach[MessageFromDevice] {

    m ⇒ {
      /* ... write to storage ... */
    }

  }
}

object Demo extends App {

  IoTHub()

    .source(java.time.Instant.now()) // <===

    .filter(Model("temperature")) // <===

    .filter(Device("device1000")) // <===

    .alsoTo(Storage()) // <===

    .to(Console())

    .run()
}
