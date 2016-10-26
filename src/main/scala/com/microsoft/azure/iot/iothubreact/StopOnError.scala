// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}

/** Akka streaming settings to stop the stream in case of errors
  *
  * @todo Review the usage of a supervisor with Akka streams
  */
case object StopOnError extends Logger {

  private[this] val decider: Supervision.Decider = {
    case e: Exception ⇒ {
      log.error(e, e.getMessage)
      Supervision.Stop
    }
  }

  implicit val actorSystem = ActorSystem("StopOnErrorStream")

  private[this] val settings = ActorMaterializerSettings(actorSystem).withSupervisionStrategy(decider)

  implicit val materializer = ActorMaterializer(settings)
}
