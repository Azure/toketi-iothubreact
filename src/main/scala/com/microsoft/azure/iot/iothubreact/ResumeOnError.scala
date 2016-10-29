// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}

/** Akka streaming settings to resume the stream in case of errors
  *
  * @todo Review the usage of a supervisor with Akka streams
  * @todo Try to remove the logger and save threads, or reuse the existing event stream
  */
case object ResumeOnError extends Logger {

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


