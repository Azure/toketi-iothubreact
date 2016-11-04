// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.sinks

import akka.Done
import akka.stream.scaladsl.Sink
import com.microsoft.azure.iot.iothubreact.{MessageToDevice, TypedSink}

import scala.concurrent.Future

/** Send messages from cloud to devices
  */
case class MessageToDeviceSink() {
  def sink(): Sink[MessageToDevice, Future[Done]] = Sink.ignore
}
