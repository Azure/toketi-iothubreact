// Copyright (c) Microsoft. All rights reserved.

// TODO: Implement once SDK is ready

package com.microsoft.azure.iot.iothubreact.sinks

import java.util.concurrent.CompletionStage

import akka.Done
import akka.stream.javadsl.{Sink ⇒ JavaSink}
import akka.stream.scaladsl.{Sink ⇒ ScalaSink}
import com.microsoft.azure.iot.iothubreact.config.{Configuration, IConfiguration}
import com.microsoft.azure.iot.iothubreact.{DeviceProperties, Logger}

object DevicePropertiesSink {
  def apply(): DevicePropertiesSink = new DevicePropertiesSink()

  def apply(config: IConfiguration): DevicePropertiesSink = new DevicePropertiesSink(config)
}

/** Define desired device state (asynchronously)
  */
class DevicePropertiesSink(config: IConfiguration)
  extends ISink[DeviceProperties]
    with Logger {

  throw new NotImplementedError("DevicePropertiesSink is not supported yet")

  // Parameterless ctor
  def this() = this(Configuration())

  def scalaSink(): ScalaSink[DeviceProperties, scala.concurrent.Future[Done]] = {
    throw new NotImplementedError()
  }

  def javaSink(): JavaSink[DeviceProperties, CompletionStage[Done]] = {
    throw new NotImplementedError()
  }
}
