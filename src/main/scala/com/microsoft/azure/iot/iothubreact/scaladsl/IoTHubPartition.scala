// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.scaladsl

import java.time.Instant

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.microsoft.azure.eventhubs.PartitionReceiver
import com.microsoft.azure.iot.iothubreact._
import com.microsoft.azure.iot.iothubreact.checkpointing._
import com.microsoft.azure.iot.iothubreact.config.IConfiguration
import com.microsoft.azure.iot.iothubreact.filters.Ignore

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
private[iothubreact] case class IoTHubPartition(config: IConfiguration, offsetLoader: IOffsetLoader, val partition: Int) extends Logger {

  /** Create a stream returning all the messages for the defined partition, from the given start
    * point, optionally with checkpointing
    *
    * @return A source of IoT messages
    */
  def source(options: SourceOptions): Source[MessageFromDevice, NotUsed] = {

    // Load the partition offset saved in the checkpoint storage
    val savedOffset = if (!options.isFromCheckpoint)
                        None
                      else {
                        val savedOffset = offsetLoader.GetSavedOffset(partition)
                        if (savedOffset.isDefined) {
                          log.info("Starting partition {} from checkpoint, offset {}", partition, savedOffset.get)
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
                       else if (options.isFromCheckpoint) savedOffset
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
    if (options.isCheckpointOnPull) {
      log.debug("Adding checkpointing flow to the partition {} stream", partition)
      source.via(new CheckpointOnPull(config.checkpointing, partition))
    } else {
      source
    }
  }

}
