// Copyright (c) Microsoft. All rights reserved.

package C_Checkpoints

import akka.stream.scaladsl.Sink
import com.microsoft.azure.iot.iothubreact.IoTMessage
import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHub

/** Retrieve messages from IoT hub and save the current position
  * In case of restart the stream starts from where it left
  * (depending on the configuration)
  */
object Demo extends App with ReactiveStreaming {

  // Sink printing to the console
  val console = Sink.foreach[IoTMessage] {
    t ⇒ println(s"Message from ${t.deviceId} - Time: ${t.created}")
  }

  // Stream
  IoTHub().source(withCheckpoints = true)
    .filter(m ⇒ m.deviceId == "device1000")
    .to(console)
    .run()
}
