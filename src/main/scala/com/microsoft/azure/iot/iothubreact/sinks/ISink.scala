// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.sinks

import akka.Done

trait ISink[A] {
  def scalaSink(): akka.stream.scaladsl.Sink[A, scala.concurrent.Future[Done]]

  def javaSink(): akka.stream.javadsl.Sink[A, java.util.concurrent.CompletionStage[Done]]
}
