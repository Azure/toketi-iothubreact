// Copyright (c) Microsoft. All rights reserved.

package F_SendMessageToDevice

import akka.stream.scaladsl.Flow
import com.microsoft.azure.iot.iothubreact.MessageToDevice
import com.microsoft.azure.iot.iothubreact.ResumeOnError._
import com.microsoft.azure.iot.iothubreact.filters.MessageType
import com.microsoft.azure.iot.iothubreact.scaladsl._

import scala.language.{implicitConversions, postfixOps}

object Demo extends App with Deserialize {

  val turnFanOn  = MessageToDevice("Turn fan ON")
  val turnFanOff = MessageToDevice("Turn fan OFF")

  val hub = IoTHub()

  // Source
  val temperatures = hub
    .source()
    .filter(MessageType("temperature"))
    .map(deserialize)

  // Too cold sink
  val tooColdWorkflow = Flow[Temperature]
    .filter(_.value < 65)
    .map(t ⇒ turnFanOff.to(t.deviceId))
    .to(hub.sink())

  // Too warm sink
  val tooWarmWorkflow = Flow[Temperature]
    .filter(_.value > 85)
    .map(t ⇒ turnFanOn.to(t.deviceId))
    .to(hub.sink())

  temperatures
    .alsoTo(tooColdWorkflow)
    .to(tooWarmWorkflow)
    .run()

  /*
  // Run the two workflows in parallel
  RunnableGraph.fromGraph(GraphDSL.create() {
    implicit b ⇒
      import GraphDSL.Implicits._

      val shape = b.add(Broadcast[Temperature](2))

      temperatures ~> shape.in

      shape.out(0) ~> tooColdWorkflow
      shape.out(1) ~> tooWarmWorkflow

      ClosedShape
  }).run()
  */
}
