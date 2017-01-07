// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}

/** Akka streaming settings to resume the stream in case of errors
  */
case object ResumeOnError extends Logger {

  // TODO: Revisit the usage of a supervisor with Akka streams
  // TODO: Try to remove the logger and save threads, or reuse the existing event stream

  private[this] val decider: Supervision.Decider = {
    case e: Exception ⇒ {
      log.error(e, e.getMessage)
      Supervision.Resume
    }
  }

  implicit val actorSystem = ActorSystem("ResumeOnErrorStream")

  private[this] val settings = ActorMaterializerSettings(actorSystem).withSupervisionStrategy(decider)

  implicit val materializer = ActorMaterializer(settings)
}


