// Copyright (c) Microsoft. All rights reserved.

// TODO: Implement once SDK is ready

package com.microsoft.azure.iot.iothubreact.sinks

import java.util.concurrent.CompletionStage

import akka.Done
import akka.stream.javadsl.{Sink ⇒ JavaSink}
import akka.stream.scaladsl.{Sink ⇒ ScalaSink}
import com.microsoft.azure.iot.iothubreact.{Logger, MethodOnDevice}

case class MethodOnDeviceSink() extends ISink[MethodOnDevice] with Logger {

  throw new NotImplementedError("MethodOnDeviceSink is not supported yet")

  def scalaSink(): ScalaSink[MethodOnDevice, scala.concurrent.Future[Done]] = {
    throw new NotImplementedError()
  }

  def javaSink(): JavaSink[MethodOnDevice, CompletionStage[Done]] = {
    throw new NotImplementedError()
  }
}
