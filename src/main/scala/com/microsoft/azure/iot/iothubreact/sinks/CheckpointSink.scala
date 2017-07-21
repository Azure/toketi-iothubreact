// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.sinks

import java.util.concurrent.CompletionStage

import akka.Done
import akka.actor.ActorRef
import akka.japi.function.Procedure
import akka.stream.javadsl.{Sink ⇒ JavaSink}
import akka.stream.scaladsl.{Sink ⇒ ScalaSink}
import com.microsoft.azure.iot.iothubreact.checkpointing.CheckpointService.CheckpointInMemory
import com.microsoft.azure.iot.iothubreact.checkpointing.{CheckpointActorSystem, IOffsetLoader}
import com.microsoft.azure.iot.iothubreact.config.IConfiguration
import com.microsoft.azure.iot.iothubreact.{Logger, MessageFromDevice}

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

private[iothubreact] final case class CheckpointSink(
    config: IConfiguration,
    offsetLoader: IOffsetLoader)
  extends ISink[MessageFromDevice]
    with Logger {

  // The service responsible for writing offsets to the storage
  lazy val checkpointService = (0 until config.connect.iotHubPartitions).map {
    p ⇒
      p → CheckpointActorSystem(config.checkpointing).getCheckpointService(p)
  }(collection.breakOut): Map[Int, ActorRef]

  // The offset stored (value) for each partition (key)
  val current: TrieMap[Int, Long] = TrieMap()

  // Initialize `current` with the offsets in the storage
  offsetLoader.GetSavedOffsets.foreach {
    case (a, c) ⇒ current += a → c.toLong
  }

  def scalaSink(): ScalaSink[MessageFromDevice, scala.concurrent.Future[Done]] = {
    ScalaSink.foreach[MessageFromDevice] {
      doWrite
    }
  }

  def javaSink(): JavaSink[MessageFromDevice, CompletionStage[Done]] = {
    JavaSink.foreach[MessageFromDevice] {
      JavaSinkProcedure
    }
  }

  // Required for Scala 2.11
  private[this] object JavaSinkProcedure extends Procedure[MessageFromDevice] {
    @scala.throws[Exception](classOf[Exception])
    override def apply(m: MessageFromDevice): Unit = {
      doWrite(m)
    }
  }

  private[this] def doWrite(m: MessageFromDevice) = {
    m.runtimeInfo.partitionInfo.partitionNumber.map {
      p ⇒
        synchronized {
          val os: Long = m.offset.toLong
          val cur: Long = current.getOrElse(p, -1)
          if (os > cur) {
            log.debug(s"Committing offset ${m.offset} on partition ${p}")
            checkpointService(p) ! CheckpointInMemory(m.offset)
            current += p → os
          } else {
            log.debug(s"Ignoring offset ${m.offset} since it precedes ${cur}")
            Future successful (Done)
          }
        }
    }
  }
}
