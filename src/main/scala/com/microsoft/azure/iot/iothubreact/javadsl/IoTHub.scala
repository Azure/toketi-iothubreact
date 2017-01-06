// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.javadsl

import java.time.Instant

import akka.NotUsed
import akka.stream.javadsl.{Source ⇒ SourceJavaDSL}
import com.microsoft.azure.iot.iothubreact.scaladsl.{IoTHub ⇒ IoTHubScalaDSL}
import com.microsoft.azure.iot.iothubreact.scaladsl.{PartitionList ⇒ PartitionListScalaDSL}
import com.microsoft.azure.iot.iothubreact.scaladsl.{OffsetList ⇒ OffsetListScalaDSL}
import com.microsoft.azure.iot.iothubreact.MessageFromDevice

/** Provides a streaming source to retrieve messages from Azure IoT Hub
  */
class IoTHub() {

  // TODO: Provide ClearCheckpoints() method to clear the state
  // TODO: Add sink and test from Java

  private lazy val iotHub = new IoTHubScalaDSL()

  /** Stop the stream
    */
  def close(): Unit = {
    iotHub.close()
  }

  /** Sink to communicate with IoT devices
    *
    * @ tparam A Type of communication (message, method, property)
    *
    * @ return Streaming sink
    */
  //def sink[A](): Sink[A, Future[Done]] = iotHub.sink[A]

  /** Stream returning all the messages since the beginning, from all the
    * configured partitions.
    *
    * @return A source of IoT messages
    */
  def source(): SourceJavaDSL[MessageFromDevice, NotUsed] = {
    new SourceJavaDSL(iotHub.source())
  }

  /** Stream returning all the messages from all the requested partitions.
    * If checkpointing the stream starts from the last position saved, otherwise
    * it starts from the beginning.
    *
    * @param partitions Partitions to process
    *
    * @return A source of IoT messages
    */
  def source(partitions: PartitionList): SourceJavaDSL[MessageFromDevice, NotUsed] = {
    new SourceJavaDSL(iotHub.source(PartitionListScalaDSL(partitions)))
  }

  /** Stream returning all the messages starting from the given time, from all
    * the configured partitions.
    *
    * @param startTime Starting position expressed in time
    *
    * @return A source of IoT messages
    */
  def source(startTime: Instant): SourceJavaDSL[MessageFromDevice, NotUsed] = {
    new SourceJavaDSL(iotHub.source(startTime))
  }

  /** Stream returning all the messages starting from the given time, from all
    * the configured partitions.
    *
    * @param startTime  Starting position expressed in time
    * @param partitions Partitions to process
    *
    * @return A source of IoT messages
    */
  def source(startTime: Instant, partitions: PartitionList): SourceJavaDSL[MessageFromDevice, NotUsed] = {
    new SourceJavaDSL(iotHub.source(startTime, PartitionListScalaDSL(partitions)))
  }

  /** Stream returning all the messages from all the configured partitions.
    * If checkpointing the stream starts from the last position saved, otherwise
    * it starts from the beginning.
    *
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    *
    * @return A source of IoT messages
    */
  def source(withCheckpoints: java.lang.Boolean): SourceJavaDSL[MessageFromDevice, NotUsed] = {
    new SourceJavaDSL(iotHub.source(withCheckpoints))
  }

  /** Stream returning all the messages from all the configured partitions.
    * If checkpointing the stream starts from the last position saved, otherwise
    * it starts from the beginning.
    *
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    * @param partitions      Partitions to process
    *
    * @return A source of IoT messages
    */
  def source(withCheckpoints: java.lang.Boolean, partitions: PartitionList): SourceJavaDSL[MessageFromDevice, NotUsed] = {
    new SourceJavaDSL(iotHub.source(withCheckpoints, PartitionListScalaDSL(partitions)))
  }

  /** Stream returning all the messages starting from the given offset, from all
    * the configured partitions.
    *
    * @param offsets Starting position for all the partitions
    *
    * @return A source of IoT messages
    */
  def source(offsets: OffsetList): SourceJavaDSL[MessageFromDevice, NotUsed] = {
    new SourceJavaDSL(iotHub.source(OffsetListScalaDSL(offsets)))
  }

  /** Stream returning all the messages starting from the given offset, from all
    * the configured partitions.
    *
    * @param offsets    Starting position for all the partitions
    * @param partitions Partitions to process
    *
    * @return A source of IoT messages
    */
  def source(offsets: OffsetList, partitions: PartitionList): SourceJavaDSL[MessageFromDevice, NotUsed] = {
    new SourceJavaDSL(iotHub.source(OffsetListScalaDSL(offsets), PartitionListScalaDSL(partitions)))
  }

  /** Stream returning all the messages starting from the given time, from all
    * the configured partitions.
    *
    * @param startTime       Starting position expressed in time
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    *
    * @return A source of IoT messages
    */
  def source(startTime: Instant, withCheckpoints: java.lang.Boolean): SourceJavaDSL[MessageFromDevice, NotUsed] = {
    new SourceJavaDSL(iotHub.source(startTime, withCheckpoints))
  }

  /** Stream returning all the messages starting from the given time, from all
    * the configured partitions.
    *
    * @param startTime       Starting position expressed in time
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    * @param partitions      Partitions to process
    *
    * @return A source of IoT messages
    */
  def source(startTime: Instant, withCheckpoints: java.lang.Boolean, partitions: PartitionList): SourceJavaDSL[MessageFromDevice, NotUsed] = {
    new SourceJavaDSL(iotHub.source(startTime, withCheckpoints, PartitionListScalaDSL(partitions)))
  }

  /** Stream returning all the messages starting from the given offset, from all
    * the configured partitions.
    *
    * @param offsets         Starting position for all the partitions
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    *
    * @return A source of IoT messages
    */
  def source(offsets: OffsetList, withCheckpoints: java.lang.Boolean): SourceJavaDSL[MessageFromDevice, NotUsed] = {
    new SourceJavaDSL(iotHub.source(OffsetListScalaDSL(offsets), withCheckpoints))
  }

  /** Stream returning all the messages starting from the given offset, from all
    * the configured partitions.
    *
    * @param offsets         Starting position for all the partitions
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    * @param partitions      Partitions to process
    *
    * @return A source of IoT messages
    */
  def source(offsets: OffsetList, withCheckpoints: java.lang.Boolean, partitions: PartitionList): SourceJavaDSL[MessageFromDevice, NotUsed] = {
    new SourceJavaDSL(iotHub.source(OffsetListScalaDSL(offsets), withCheckpoints, PartitionListScalaDSL(partitions)))
  }
}
