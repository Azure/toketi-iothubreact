// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.test.helpers

import java.util.concurrent.Executors

import akka.actor.Actor
import akka.actor.{Actor, Stash}

import scala.concurrent.ExecutionContext

/* Thread safe counter */
class Counter extends Actor with Stash {

  implicit val executionContext = ExecutionContext
    .fromExecutorService(Executors.newFixedThreadPool(sys.runtime.availableProcessors))

  private[this] var count: Long = 0

  override def receive: Receive = ready

  def ready: Receive = {
    case "reset" ⇒ {
      context.become(busy)
      count = 0
      context.become(ready)
      unstashAll()
    }
    case "inc"   ⇒ {
      context.become(busy)
      count += 1
      context.become(ready)
      unstashAll()
    }
    case "get"   ⇒ sender() ! count
  }

  def busy: Receive = {
    case _ ⇒ stash()
  }
}
