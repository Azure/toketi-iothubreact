// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.sinks

import akka.Done
import akka.stream.scaladsl.Sink
import com.microsoft.azure.iot.iothubreact.{DeviceProperties, TypedSink}

import scala.concurrent.Future

case class DevicePropertiesSink() {
  def sink(): Sink[DeviceProperties, Future[Done]] = Sink.ignore
}
