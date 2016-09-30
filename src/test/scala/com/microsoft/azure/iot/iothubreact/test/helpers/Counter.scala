// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.test.helpers

import java.util.concurrent.Executors

import akka.actor.Actor

import scala.concurrent.ExecutionContext

/* Thread safe counter */
class Counter extends Actor {

  implicit val executionContext = ExecutionContext
    .fromExecutorService(Executors.newFixedThreadPool(sys.runtime.availableProcessors))

  private[this] var count: Long = 0

  override def receive: Receive = {
    case "reset" ⇒ count = 0
    case "inc"   ⇒ count += 1
    case "get"   ⇒ sender() ! count
  }
}
