// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}

/** Akka streaming settings to stop the stream in case of errors
  */
case object StopOnError extends Logger {

  // TODO: Review the usage of a supervisor with Akka streams
  // TODO: Try to remove the logger and save threads, or reuse the existing event stream

  private[this] val decider: Supervision.Decider = {
    case e: Exception ⇒ {
      log.error(e, e.getMessage)
      e.printStackTrace()
      Supervision.Stop
    }
  }

  implicit val actorSystem = ActorSystem("StopOnErrorStream")

  private[this] val settings = ActorMaterializerSettings(actorSystem).withSupervisionStrategy(decider)

  implicit val materializer = ActorMaterializer(settings)
}
