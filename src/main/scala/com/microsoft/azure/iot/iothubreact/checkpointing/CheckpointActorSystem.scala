// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.checkpointing

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}

import scala.language.{implicitConversions, postfixOps}

/** The actors infrastructure for storing the stream position
  */
private[iothubreact] object CheckpointActorSystem {

  implicit private[this] val actorSystem  = ActorSystem("IoTHubReact")
  implicit private[this] val materializer = ActorMaterializer(ActorMaterializerSettings(actorSystem))
  implicit private[this] val cpconfig     = new CPConfiguration
  var localRegistry: Map[String, ActorRef] = Map[String, ActorRef]()

  /** Create an actor to read/write offset checkpoints from the storage
    *
    * @param partition IoT hub partition number
    *
    * @return Actor reference
    */
  def getCheckpointService(partition: Int): ActorRef = {
    val actorPath = "CheckpointService" + partition

    localRegistry get actorPath match {
      case Some(actorRef) ⇒ actorRef
      case None           ⇒ {
        val actorRef = actorSystem.actorOf(Props(new CheckpointService(partition)), actorPath)
        localRegistry += Tuple2(actorPath, actorRef)
        actorRef
      }
    }
  }
}
