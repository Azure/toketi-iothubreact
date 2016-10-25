// Copyright (c) Microsoft. All rights reserved.

package A_OutputMessagesToConsole

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

  implicit val actorSystem  = ActorSystem("Demo")
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(actorSystem).withSupervisionStrategy(decider))
}
