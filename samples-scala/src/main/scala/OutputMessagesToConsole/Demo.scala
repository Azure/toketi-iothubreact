// Copyright (c) Microsoft. All rights reserved.

package OutputMessagesToConsole

import akka.stream.scaladsl.Sink
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.microsoft.azure.iot.iothubreact.filters.Schema
import com.microsoft.azure.iot.iothubreact.scaladsl.{IoTHub, IoTHubPartition}

import scala.language.{implicitConversions, postfixOps}

/** Retrieve messages from IoT hub and display the data sent from Temperature devices
  */
object Demo extends App with ReactiveStreaming {

  // Source retrieving messages from all IoT hub partitions
  val messagesFromAllPartitions = IoTHub().source()

  // Source retrieving messages from one IoT hub partition (e.g. partition 2)
  val messagesFromOnePartition = IoTHubPartition(2).source()

  // Source retrieving only recent messages
  val messagesFromNowOn = IoTHub().source(java.time.Instant.now())

  // Sink printing to the console
  val console = Sink.foreach[Temperature] {
    t ⇒ println(s"Device ${t.deviceId}: temperature: ${t.value}C ; T=${t.time}")
  }

  // JSON parser setup
  val jsonParser = new ObjectMapper()
  jsonParser.registerModule(DefaultScalaModule)

  // Stream
  messagesFromAllPartitions
    .filter(Schema("temperature"))
    .map(m ⇒ {
      val temperature = jsonParser.readValue(m.contentAsString, classOf[Temperature])
      temperature.deviceId = m.deviceId
      temperature
    })
    .to(console)
    .run()
}
