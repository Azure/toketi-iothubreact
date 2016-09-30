// Copyright (c) Microsoft. All rights reserved.

package OutputMessagesToConsole

import akka.stream.scaladsl.Sink
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHub

/** Retrieve messages from IoT hub and display the data sent from Temperature
  * devices
  */
object Demo extends App with ReactiveStreaming {

  // Source retrieving messages from all IoT hub partitions
  val messagesFromAllPartitions = new IoTHub().source()

  // Source retrieving only recent messages
  // import java.time.Instant
  // val messagesFromNowOn = new IoTHub().source(Instant.now())

  // Source retrieving messages from one IoT hub partition (0 to N-1, where N is
  // defined at deployment time)
  // val messagesFromOnePartition = new IoTHub().source(PARTITION)

  // Sink printing to the console
  val console = Sink.foreach[Temperature] {
    t ⇒ println(s"Device ${t.deviceId}: temperature: ${t.value}F ; T=${t.time}")
  }

  // JSON parser setup
  val jsonParser = new ObjectMapper()
  jsonParser.registerModule(DefaultScalaModule)

  // Start processing the stream
  messagesFromAllPartitions
    .map(m ⇒ {
      val temperature = jsonParser.readValue(m.contentAsString, classOf[Temperature])
      temperature.deviceId = m.deviceId
      temperature
    })
    .to(console)
    .run()
}
