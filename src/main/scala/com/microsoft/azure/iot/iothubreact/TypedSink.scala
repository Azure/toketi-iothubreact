// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

import java.util.concurrent.CompletionStage

import akka.Done
import akka.stream.javadsl.{Sink ⇒ JavaSink}
import akka.stream.scaladsl.{Sink ⇒ ScalaSink}
import com.microsoft.azure.iot.iothubreact.sinks._

import scala.concurrent.Future

/** Type class to support different classes of communication through IoTHub
  *
  * @tparam A
  */
trait TypedSink[A] {
  def scalaDefinition: ScalaSink[A, Future[Done]]

  def javaDefinition: JavaSink[A, CompletionStage[Done]]
}

/** Type class implementations for MessageToDevice, MethodOnDevice, DeviceProperties
  * Automatically selects the appropriate sink depending on type of communication.
  */
object TypedSink {

  implicit object MessageToDeviceSinkDef extends TypedSink[MessageToDevice] {
    override def scalaDefinition: ScalaSink[MessageToDevice, Future[Done]] = MessageToDeviceSink().scalaSink()

    override def javaDefinition: JavaSink[MessageToDevice, CompletionStage[Done]] = MessageToDeviceSink().javaSink()
  }

  implicit object MethodOnDeviceSinkDef extends TypedSink[MethodOnDevice] {
    override def scalaDefinition: ScalaSink[MethodOnDevice, Future[Done]] = MethodOnDeviceSink().scalaSink()

    override def javaDefinition: JavaSink[MethodOnDevice, CompletionStage[Done]] = MethodOnDeviceSink().javaSink()
  }

  implicit object DevicePropertiesSinkDef extends TypedSink[DeviceProperties] {
    override def scalaDefinition: ScalaSink[DeviceProperties, Future[Done]] = DevicePropertiesSink().scalaSink()

    override def javaDefinition: JavaSink[DeviceProperties, CompletionStage[Done]] = DevicePropertiesSink().javaSink()
  }

}
