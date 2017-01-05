// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

import akka.Done
import akka.stream.scaladsl.Sink
import com.microsoft.azure.iot.iothubreact.sinks.MessageToDeviceSink

import scala.concurrent.Future

/** Type class to support different classes of communication through IoTHub
  *
  * @tparam A
  */
trait TypedSink[A] {
  def definition: Sink[A, Future[Done]]
}

/** Type class implementations for MessageToDevice, MethodOnDevice, DeviceProperties
  */
object TypedSink {

  implicit object MessageToDeviceSinkDef extends TypedSink[MessageToDevice] {
    override def definition: Sink[MessageToDevice, Future[Done]] = MessageToDeviceSink().sink()
  }

  /*implicit object MethodOnDeviceSinkDef extends TypedSink[MethodOnDevice] {
    override def definition: Sink[MethodOnDevice, Future[Done]] = MethodOnDeviceSink().sink()
  }

  implicit object DevicePropertiesSinkDef extends TypedSink[DeviceProperties] {
    override def definition: Sink[DeviceProperties, Future[Done]] = DevicePropertiesSink().sink()
  }*/
}
