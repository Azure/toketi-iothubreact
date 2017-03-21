// Copyright (c) Microsoft. All rights reserved.

// TODO: Implement once SDK is ready

package com.microsoft.azure.iot.iothubreact.sinks

import java.util.concurrent.CompletionStage

import akka.Done
import akka.stream.javadsl.{Sink ⇒ JavaSink}
import akka.stream.scaladsl.{Sink ⇒ ScalaSink}
import com.microsoft.azure.iot.iothubreact.{Configuration, IConfiguration, Logger, MethodOnDevice}

object MethodOnDeviceSink {
  def apply(): MethodOnDeviceSink = new MethodOnDeviceSink()

  def apply(config: IConfiguration): MethodOnDeviceSink = new MethodOnDeviceSink(config)
}

/** Invoke methods (synchronously) from cloud to connected devices
  */
class MethodOnDeviceSink(config: IConfiguration)
  extends ISink[MethodOnDevice]
    with Logger {

  // Parameterless ctor
  def this() = this(Configuration())

  throw new NotImplementedError("MethodOnDeviceSink is not supported yet")

  def scalaSink(): ScalaSink[MethodOnDevice, scala.concurrent.Future[Done]] = {
    throw new NotImplementedError()
  }

  def javaSink(): JavaSink[MethodOnDevice, CompletionStage[Done]] = {
    throw new NotImplementedError()
  }
}
