// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.test.helpers

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}

/** Initialize reactive streaming
  *
  * @todo Don't use supervisor with Akka streams
  */
trait ReactiveStreaming {
  val decider: Supervision.Decider = {
    case e: Exception ⇒
      println(e.getMessage)
      Supervision.Resume
  }

  implicit val actorSystem  = ActorSystem("Tests")
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(actorSystem)
    .withSupervisionStrategy(decider))
}
