// Copyright (c) Microsoft. All rights reserved.

package E_Checkpoints

import akka.stream.scaladsl.Sink
import com.microsoft.azure.iot.iothubreact.{MessageFromDevice, SourceOptions}
import com.microsoft.azure.iot.iothubreact.ResumeOnError._
import com.microsoft.azure.iot.iothubreact.filters.Device
import com.microsoft.azure.iot.iothubreact.scaladsl._

/** Retrieve messages from IoT hub and save the current position
  * In case of restart the stream starts from where it left
  * (depending on the configuration)
  *
  * Note, the demo requires Cassandra, you can start an instance with Docker:
  * # docker run -ip 9042:9042 --rm cassandra
  */
object Demo extends App {

  val console = Sink.foreach[MessageFromDevice] {
    t ⇒ println(s"Message from ${t.deviceId} - Time: ${t.received}")
  }

  // Stream using checkpointing
  IoTHub().source(SourceOptions().checkpointOnPull)
    .filter(Device("device1000"))
    .to(console)
    .run()
}
