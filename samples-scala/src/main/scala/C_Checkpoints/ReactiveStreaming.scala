// Copyright (c) Microsoft. All rights reserved.

package C_Checkpoints

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

  implicit val actorSystem = ActorSystem("Demo")
  val settings = ActorMaterializerSettings(actorSystem).withSupervisionStrategy(decider)
  implicit val materializer = ActorMaterializer(settings)
}
