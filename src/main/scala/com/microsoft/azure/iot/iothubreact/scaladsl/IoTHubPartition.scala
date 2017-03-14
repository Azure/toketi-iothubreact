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
import com.microsoft.azure.iot.iothubreact.checkpointing.{CheckpointActorSystem, SavePositionOnPull, Configuration ⇒ CPConfiguration}
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
private[iothubreact] case class IoTHubPartition(val partition: Int) extends Logger {

  /** Stream returning all the messages from the given offset
    *
    * @param startTime       Starting position expressed in time
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    *
    * @return A source of IoT messages
    */
  def source(startTime: Instant, withCheckpoints: Boolean): Source[MessageFromDevice, NotUsed] = {
    getSource(
      withTimeOffset = true,
      startTime = startTime,
      withCheckpoints = withCheckpoints && CPConfiguration.isEnabled)
  }

  /** Stream returning all the messages from the given offset
    *
    * @param offset          Starting position, offset of the first message
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    *
    * @return A source of IoT messages
    */
  def source(offset: String, withCheckpoints: Boolean): Source[MessageFromDevice, NotUsed] = {
    getSource(
      withTimeOffset = false,
      offset = offset,
      withCheckpoints = withCheckpoints && CPConfiguration.isEnabled)
  }

  /** Create a stream returning all the messages for the defined partition, from the given start
    * point, optionally with checkpointing
    *
    * @param withTimeOffset  Whether the start point is a timestamp
    * @param offset          Starting position using the offset property in the messages
    * @param startTime       Starting position expressed in time
    * @param withCheckpoints Whether to read/write the stream position
    *
    * @return A source of IoT messages
    */
  private[this] def getSource(
      withTimeOffset: Boolean,
      offset: String = "",
      startTime: Instant = Instant.MIN,
      withCheckpoints: Boolean = true): Source[MessageFromDevice, NotUsed] = {

    // Load the offset from the storage (if needed)
    var _offset = offset
    var _withTimeOffset = withTimeOffset
    if (withCheckpoints) {
      val savedOffset = GetSavedOffset()
      if (savedOffset != IoTHubPartition.OffsetCheckpointNotFound) {
        _offset = savedOffset
        _withTimeOffset = false
        log.info(s"Starting partition ${partition} from saved offset ${_offset}")
      }
    }

    // Build the source starting by time or by offset
    val source: Source[MessageFromDevice, NotUsed] = if (_withTimeOffset)
                                                       MessageFromDeviceSource(partition, startTime, withCheckpoints).filter(Ignore.keepAlive)
                                                     else
                                                       MessageFromDeviceSource(partition, _offset, withCheckpoints).filter(Ignore.keepAlive)

    // Inject a flow to store the stream position after each pull
    if (withCheckpoints) {
      log.debug(s"Adding checkpointing flow to the partition ${partition} stream")
      source.via(new SavePositionOnPull(partition))
    } else {
      source
    }
  }

  /** Get the offset saved for the current partition
    *
    * @return Offset
    */
  private[this] def GetSavedOffset(): String = {
    val partitionCp = CheckpointActorSystem.getCheckpointService(partition)
    implicit val rwTimeout = Timeout(CPConfiguration.checkpointRWTimeout)
    try {
      Retry(3, 5 seconds) {
        log.debug(s"Loading the stream position for partition ${partition}")
        val future = (partitionCp ? GetOffset).mapTo[String]
        Await.result(future, rwTimeout.duration)
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
