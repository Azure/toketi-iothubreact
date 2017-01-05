// Copyright (c) Microsoft. All rights reserved.

// TODO: Develop once SDK is ready

package com.microsoft.azure.iot.iothubreact.sinks

import akka.Done
import akka.stream.scaladsl.Sink
import com.microsoft.azure.iot.iothubreact.MethodOnDevice

import scala.concurrent.Future

case class MethodOnDeviceSink() {

  throw new NotImplementedError("DevicePropertiesSink is not supported yet")

  def sink(): Sink[MethodOnDevice, Future[Done]] = Sink.foreach[MethodOnDevice] {
    m ⇒ ???
  }
}
