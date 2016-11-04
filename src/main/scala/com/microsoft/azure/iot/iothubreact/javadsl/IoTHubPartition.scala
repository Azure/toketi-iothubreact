// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.javadsl

import java.time.Instant

import akka.NotUsed
import akka.stream.javadsl.{Source ⇒ SourceJavaDSL}
import com.microsoft.azure.iot.iothubreact.{MessageFromDevice, Offset}
import com.microsoft.azure.iot.iothubreact.scaladsl.{IoTHubPartition ⇒ IoTHubPartitionScalaDSL}

/** Provides a streaming source to retrieve messages from one Azure IoT Hub partition
  *
  * @param partition IoT hub partition number (0-based). The number of
  *                  partitions is set during the deployment.
  *
  * @todo (*) Provide ClearCheckpoints() method to clear the state
  * @todo Support reading the same partition from multiple clients
  */
class IoTHubPartition(val partition: Int) {

  // Offset used to start reading from the beginning
  final val OffsetStartOfStream: String = IoTHubPartitionScalaDSL.OffsetStartOfStream

  // Public constant: used internally to signal when there is no position saved in the storage
  // To be used by custom backend implementations
  final val OffsetCheckpointNotFound: String = IoTHubPartitionScalaDSL.OffsetCheckpointNotFound

  private lazy val iotHubPartition = new IoTHubPartitionScalaDSL(partition)

  /** Stream returning all the messages since the beginning, from the specified
    * partition.
    *
    * @return A source of IoT messages
    */
  def source(): SourceJavaDSL[MessageFromDevice, NotUsed] = {
    new SourceJavaDSL(iotHubPartition.source())
  }

  /** Stream returning all the messages from the given offset, from the
    * specified partition.
    *
    * @param startTime Starting position expressed in time
    *
    * @return A source of IoT messages
    */
  def source(startTime: Instant): SourceJavaDSL[MessageFromDevice, NotUsed] = {
    new SourceJavaDSL(iotHubPartition.source(startTime))
  }

  /** Stream returning all the messages. If checkpointing, the stream starts from the last position
    * saved, otherwise it starts from the beginning.
    *
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    *
    * @return A source of IoT messages
    */
  def source(withCheckpoints: Boolean): SourceJavaDSL[MessageFromDevice, NotUsed] = {
    new SourceJavaDSL(iotHubPartition.source(withCheckpoints))
  }

  /** Stream returning all the messages from the given offset, from the
    * specified partition.
    *
    * @param offset Starting position, offset of the first message
    *
    * @return A source of IoT messages
    */
  def source(offset: Offset): SourceJavaDSL[MessageFromDevice, NotUsed] = {
    new SourceJavaDSL(iotHubPartition.source(offset))
  }

  /** Stream returning all the messages from the given offset
    *
    * @param startTime       Starting position expressed in time
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    *
    * @return A source of IoT messages
    */
  def source(startTime: Instant, withCheckpoints: Boolean): SourceJavaDSL[MessageFromDevice, NotUsed] = {
    new SourceJavaDSL(iotHubPartition.source(startTime, withCheckpoints))
  }

  /** Stream returning all the messages from the given offset
    *
    * @param offset          Starting position, offset of the first message
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    *
    * @return A source of IoT messages
    */
  def source(offset: Offset, withCheckpoints: Boolean): SourceJavaDSL[MessageFromDevice, NotUsed] = {
    new SourceJavaDSL(iotHubPartition.source(offset, withCheckpoints))
  }
}
