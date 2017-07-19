// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

import java.util.concurrent.CompletionStage

import akka.Done
import akka.stream.javadsl.{Sink ⇒ JavaSink}
import akka.stream.scaladsl.{Sink ⇒ ScalaSink}
import com.microsoft.azure.iot.iothubreact.config.IConfiguration
import com.microsoft.azure.iot.iothubreact.sinks._

import scala.concurrent.Future

/** Type class to support different classes of communication through IoTHub
  *
  * How it works: when using hub.sink(), the library will automatically select
  * the right sink below, depending on the incoming object class. For instance,
  * if hub.sink() receives a `MethodOnDevice` then the library automatically
  * routes the object to `MethodOnDeviceSink`.
  *
  * @tparam A
  */
trait TypedSink[A] {
  def scalaDefinition(config: IConfiguration): ScalaSink[A, Future[Done]]

  def javaDefinition(config: IConfiguration): JavaSink[A, CompletionStage[Done]]
}

/** Type class implementations for MessageToDevice, MethodOnDevice, DeviceProperties
  * Automatically selects the appropriate sink depending on type of communication.
  */
object TypedSink {

  implicit object MessageToDeviceSinkDef extends TypedSink[MessageToDevice] {
    override def scalaDefinition(config: IConfiguration): ScalaSink[MessageToDevice, Future[Done]] = MessageToDeviceSink(config.connect).scalaSink()

    override def javaDefinition(config: IConfiguration): JavaSink[MessageToDevice, CompletionStage[Done]] = MessageToDeviceSink(config.connect).javaSink()
  }

  implicit object MethodOnDeviceSinkDef extends TypedSink[MethodOnDevice] {
    override def scalaDefinition(config: IConfiguration): ScalaSink[MethodOnDevice, Future[Done]] = MethodOnDeviceSink(config).scalaSink()

    override def javaDefinition(config: IConfiguration): JavaSink[MethodOnDevice, CompletionStage[Done]] = MethodOnDeviceSink(config).javaSink()
  }

  implicit object DevicePropertiesSinkDef extends TypedSink[DeviceProperties] {
    override def scalaDefinition(config: IConfiguration): ScalaSink[DeviceProperties, Future[Done]] = DevicePropertiesSink(config).scalaSink()

    override def javaDefinition(config: IConfiguration): JavaSink[DeviceProperties, CompletionStage[Done]] = DevicePropertiesSink(config).javaSink()
  }

}
