// Copyright (c) Microsoft. All rights reserved.

package F_SendMessageToDevice

import akka.stream.ClosedShape
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph}
import com.microsoft.azure.iot.iothubreact.{MessageFromDevice, MessageToDevice}
import com.microsoft.azure.iot.iothubreact.ResumeOnError._
import com.microsoft.azure.iot.iothubreact.filters.Model
import com.microsoft.azure.iot.iothubreact.scaladsl._
import org.json4s._
import org.json4s.jackson.JsonMethods._


import scala.language.{implicitConversions, postfixOps}

object Demo extends App {

  def deserialize(m: MessageFromDevice): Temperature = {
    implicit val formats = DefaultFormats
    val temperature = parse(m.contentAsString).extract[Temperature]
    temperature.deviceId = m.deviceId
    temperature
  }

  val turnFanOff = MessageToDevice("Turn fan OFF")
  val turnFanOn  = MessageToDevice("Turn fan ON")

  // more APIs
  // val turnFanOn = IoTDeviceMessage("Turn fan ON")
  //     .expiry(Instant.now().plusSeconds(60))
  //     .ack(DeliveryAcknowledgement.Full)
  //     .addProperty("speed", "high")

  // Hub
  val hub = IoTHub()

  // Source
  val temperatures = hub
    .source()
    .filter(Model("temperature"))
    .map(deserialize)

  // Sink 1
  val tooColdWorkflow = Flow[Temperature]
    .filter(_.value < 65)
    .map(t ⇒ turnFanOff.to(t.deviceId))
    .to(hub.sink())

  // Sink 2
  val tooWarmWorkflow = Flow[Temperature]
    .filter(_.value > 85)
    .map(t ⇒ turnFanOn.to(t.deviceId))
    .to(hub.sink())


  // Option 1: run the two workflows in series

  temperatures
    .alsoTo(tooColdWorkflow)
    .to(tooWarmWorkflow)
    .run()

  // Option 2: run the two workflows in parallel

  RunnableGraph.fromGraph(GraphDSL.create() {
    implicit b =>
      import GraphDSL.Implicits._

      val shape = b.add(Broadcast[Temperature](2))

      temperatures ~> shape.in

      shape.out(0) ~> tooColdWorkflow
      shape.out(1) ~> tooWarmWorkflow

      ClosedShape
  }).run()
}
