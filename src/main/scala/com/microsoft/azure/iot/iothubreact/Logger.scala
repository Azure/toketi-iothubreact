// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

import akka.actor.ActorSystem
import akka.event.{LogSource, Logging}

private[iothubreact] object Logger {
  val actorSystem = ActorSystem("IoTHubReact")
}

/** Common logger via Akka
  *
  * @see http://doc.akka.io/docs/akka/2.4.10/scala/logging.html
  */
private[iothubreact] trait Logger {

  implicit val logSource = new LogSource[AnyRef] {
    def genString(o: AnyRef): String = o.getClass.getName

    override def getClazz(o: AnyRef): Class[_] = o.getClass
  }

  val log = Logging(Logger.actorSystem, this)
}
