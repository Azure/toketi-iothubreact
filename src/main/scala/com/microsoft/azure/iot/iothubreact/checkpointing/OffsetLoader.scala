package com.microsoft.azure.iot.iothubreact.checkpointing

import akka.util.Timeout
import com.microsoft.azure.iot.iothubreact.{Logger, Retry}
import com.microsoft.azure.iot.iothubreact.checkpointing.CheckpointService.GetOffset
import com.microsoft.azure.iot.iothubreact.config.IConfiguration
import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHubPartition

import scala.concurrent.Await

trait IOffsetLoader {
  private[iothubreact] def GetSavedOffset(partition: Int): Option[String]
  private[iothubreact] def GetSavedOffsets: Map[Int, String]
}

class OffsetLoader(config: IConfiguration) extends IOffsetLoader with Logger {

  /** Get the offset saved for the current partition
    *
    * @return Offset
    */
  private[iothubreact] def GetSavedOffset(partition: Int): Option[String] = {
    import scala.language.postfixOps
    import scala.concurrent.duration._
    import akka.pattern.ask
    val partitionCp = CheckpointActorSystem(config.checkpointing).getCheckpointService(partition)
    implicit val rwTimeout = Timeout(config.checkpointing.checkpointRWTimeout)
    try {
      Retry(3, 5 seconds) {
        log.debug("Loading the stream offset for partition {}", partition)
        val future = (partitionCp ? GetOffset).mapTo[String]
        val offset = Await.result(future, rwTimeout.duration)
        if (offset != IoTHubPartition.OffsetCheckpointNotFound) Some(offset) else None
      }
    } catch {
      case e: java.util.concurrent.TimeoutException ⇒
        log.error(e, "Timeout while retrieving the offset from the storage")
        throw e
      case e: Exception ⇒
        log.error(e, e.getMessage)
        throw e
    }
  }

  private[iothubreact] def GetSavedOffsets: Map[Int, String] = {
    (0 to config.connect.iotHubPartitions).flatMap { p ⇒
      GetSavedOffset(p).map { o ⇒
        p → o
      }
    }(collection.breakOut)
  }

}
