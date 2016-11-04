// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.sinks

import akka.Done
import akka.stream.scaladsl.Sink
import com.microsoft.azure.iot.iothubreact.{MethodOnDevice, TypedSink}

import scala.concurrent.Future

case class MethodOnDeviceSink() {
  def sink(): Sink[MethodOnDevice, Future[Done]] = Sink.ignore
}
