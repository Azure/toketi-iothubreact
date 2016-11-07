// Copyright (c) Microsoft. All rights reserved.

package it.helpers

import akka.actor.ActorSystem
import akka.event.{LogSource, Logging}

object Logger {
  val actorSystem = ActorSystem("IoTHubReactTests")
}

/** Common logger via Akka
  *
  * @see http://doc.akka.io/docs/akka/2.4.10/scala/logging.html
  */
trait Logger {

  implicit val logSource = new LogSource[AnyRef] {
    def genString(o: AnyRef): String = o.getClass.getName

    override def getClazz(o: AnyRef): Class[_] = o.getClass
  }

  val log = Logging(Logger.actorSystem, this)
}
