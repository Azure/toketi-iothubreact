// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.scaladsl

import java.time.Instant

import akka.NotUsed
import akka.pattern.ask
import akka.stream.scaladsl.Source
import akka.util.Timeout
import com.microsoft.azure.eventhubs.PartitionReceiver
import com.microsoft.azure.iot.iothubreact._
import com.microsoft.azure.iot.iothubreact.checkpointing.CheckpointService.GetOffset
import com.microsoft.azure.iot.iothubreact.checkpointing.{CheckpointActorSystem, SaveOffsetOnPull}
import com.microsoft.azure.iot.iothubreact.config.IConfiguration
import com.microsoft.azure.iot.iothubreact.filters.Ignore

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}

object IoTHubPartition extends Logger {

  // Public constant: offset position used to start reading from the beginning
  final val OffsetStartOfStream: String = PartitionReceiver.START_OF_STREAM

  // Public constant: used internally to signal when there is no position saved in the storage
  // To be used by custom backend implementations
  final val OffsetCheckpointNotFound: String = "{offset checkpoint not found}"
}

/** Provide a streaming source to retrieve messages from one Azure IoT Hub partition
  *
  * @param partition IoT hub partition number (0-based). The number of
  *                  partitions is set during the deployment.
  */
private[iothubreact] case class IoTHubPartition(config: IConfiguration, val partition: Int) extends Logger {

  /** Create a stream returning all the messages for the defined partition, from the given start
    * point, optionally with checkpointing
    *
    * @return A source of IoT messages
    */
  def source(options: SourceOptions): Source[MessageFromDevice, NotUsed] = {

    // Load the partition offset saved in the checkpoint storage
    val savedOffset = if (!options.isFromSavedOffsets)
                        None
                      else {
                        val savedOffset = GetSavedOffset
                        if (savedOffset.isDefined) {
                          log.info("Starting partition {} from saved offset {}", partition, savedOffset.get)
                          savedOffset
                        } else if (options.getStartTimeOnNoCheckpoint.isEmpty) {
                          // The user didn't provide a start time for missing
                          // checkpoints, so let's start from the beginning
                          Some(IoTHubPartition.OffsetStartOfStream)
                        } else {
                          // The user didn't provide a start time for missing
                          // checkpoints, but provided a start time for such case
                          // so we leave this empty
                          None
                        }
                      }

    // Define the start point offset
    val startOffsets = if (options.isFromStart) Some(IoTHubPartition.OffsetStartOfStream)
                       else if (options.isFromOffsets) Some(options.getStartOffsets(config.connect)(partition))
                       else if (options.isFromSavedOffsets) savedOffset
                       else if (options.isFromTime) None
                       else None

    // Decide whether to start streaming from a time or an offset
    val withTimeOffset = if (options.isFromTime) true
                         else if (startOffsets.isDefined) false
                         else if (options.getStartTimeOnNoCheckpoint.isDefined) true
                         else false

    // Define the start point timestamp
    val startTime = if (options.isFromTime) options.getStartTime.get
                    else if (withTimeOffset) options.getStartTimeOnNoCheckpoint.get
                    else Instant.MIN

    // Build the source starting by time or by offset
    val source = if (withTimeOffset)
                   MessageFromDeviceSource(config, partition, startTime).filter(Ignore.keepAlive)
                 else
                   MessageFromDeviceSource(config, partition, startOffsets.get).filter(Ignore.keepAlive)

    // Inject a flow to store the stream position after each pull
    if (options.isSaveOffsetsOnPull) {
      log.debug("Adding checkpointing flow to the partition {} stream", partition)
      source.via(new SaveOffsetOnPull(config.checkpointing, partition))
    } else {
      source
    }
  }

  /** Get the offset saved for the current partition
    *
    * @return Offset
    */
  private[this] def GetSavedOffset(): Option[String] = {
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
}
