// Copyright (c) Microsoft. All rights reserved.

package B_PrintTemperature

import akka.stream.scaladsl.Sink
import com.microsoft.azure.iot.iothubreact.IoTMessage
import com.microsoft.azure.iot.iothubreact.ResumeOnError._
import com.microsoft.azure.iot.iothubreact.filters.Model
import com.microsoft.azure.iot.iothubreact.scaladsl._
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.language.{implicitConversions, postfixOps}

/** Retrieve messages from IoT hub and display the data sent from Temperature devices
  *
  * Show how to deserialize content (JSON)
  */
object Demo extends App {

  def deserialize(m: IoTMessage): Temperature = {

    // JSON parser setup, brings in default date formats etc.
    implicit val formats = DefaultFormats

    val temperature = parse(m.contentAsString).extract[Temperature]
    temperature.deviceId = m.deviceId
    temperature
  }

  val messages = IoTHub().source()

  // Sink printing to the console
  val console = Sink.foreach[Temperature] {
    t ⇒ println(s"Device ${t.deviceId}: temperature: ${t.value}C ; T=${t.datetime}")
  }

  // Stream
  messages

    // Equivalent to: m ⇒ m.model == "temperature"
    .filter(Model("temperature"))

    // Deserialize JSON
    .map(m ⇒ deserialize(m))

    // Send Temperature object to the console sink
    .to(console)
    .run()
}

