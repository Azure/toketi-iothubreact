// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.javadsl

import java.time.Instant
import java.util.concurrent.CompletionStage

import akka.stream.javadsl.{Sink, Source ⇒ JavaSource}
import akka.{Done, NotUsed}
import com.microsoft.azure.iot.iothubreact._
import com.microsoft.azure.iot.iothubreact.scaladsl.{IoTHub ⇒ IoTHubScalaDSL, OffsetList ⇒ OffsetListScalaDSL, PartitionList ⇒ PartitionListScalaDSL}
import com.microsoft.azure.iot.iothubreact.sinks.{DevicePropertiesSink, MessageToDeviceSink, MethodOnDeviceSink}

/** Provides a streaming source to retrieve messages from Azure IoT Hub
  *
  * TODO: Provide ClearCheckpoints() method to clear the state
  */
class IoTHub(config: IConfiguration) {

  // Parameterless ctor
  def this() = this(Configuration())

  private lazy val iotHub = IoTHubScalaDSL(config)

  /** Stop the stream
    */
  def close(): Unit = {
    iotHub.close()
  }

  /** Sink to send asynchronous messages to IoT devices
    *
    * @return Streaming sink
    */
  def messageSink: Sink[MessageToDevice, CompletionStage[Done]] =
    MessageToDeviceSink().javaSink()

  /** Sink to call synchronous methods on IoT devices
    *
    * @return Streaming sink
    */
  def methodSink: Sink[MethodOnDevice, CompletionStage[Done]] =
    MethodOnDeviceSink().javaSink()

  /** Sink to asynchronously set properties on IoT devices
    *
    * @return Streaming sink
    */
  def propertySink: Sink[DeviceProperties, CompletionStage[Done]] =
    DevicePropertiesSink().javaSink()

  /** Stream returning all the messages since the beginning, from all the
    * configured partitions.
    *
    * @return A source of IoT messages
    */
  def source(): JavaSource[MessageFromDevice, NotUsed] = {
    new JavaSource(iotHub.source())
  }

  /** Stream returning all the messages from all the requested partitions.
    * If checkpointing the stream starts from the last position saved, otherwise
    * it starts from the beginning.
    *
    * @param partitions Partitions to process
    *
    * @return A source of IoT messages
    */
  def source(partitions: PartitionList): JavaSource[MessageFromDevice, NotUsed] = {
    new JavaSource(iotHub.source(PartitionListScalaDSL(partitions)))
  }

  /** Stream returning all the messages starting from the given time, from all
    * the configured partitions.
    *
    * @param startTime Starting position expressed in time
    *
    * @return A source of IoT messages
    */
  def source(startTime: Instant): JavaSource[MessageFromDevice, NotUsed] = {
    new JavaSource(iotHub.source(startTime))
  }

  /** Stream returning all the messages starting from the given time, from all
    * the configured partitions.
    *
    * @param startTime  Starting position expressed in time
    * @param partitions Partitions to process
    *
    * @return A source of IoT messages
    */
  def source(startTime: Instant, partitions: PartitionList): JavaSource[MessageFromDevice, NotUsed] = {
    new JavaSource(iotHub.source(startTime, PartitionListScalaDSL(partitions)))
  }

  /** Stream returning all the messages from all the configured partitions.
    * If checkpointing the stream starts from the last position saved, otherwise
    * it starts from the beginning.
    *
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    *
    * @return A source of IoT messages
    */
  def source(withCheckpoints: java.lang.Boolean): JavaSource[MessageFromDevice, NotUsed] = {
    new JavaSource(iotHub.source(withCheckpoints))
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
  def source(withCheckpoints: java.lang.Boolean, partitions: PartitionList): JavaSource[MessageFromDevice, NotUsed] = {
    new JavaSource(iotHub.source(withCheckpoints, PartitionListScalaDSL(partitions)))
  }

  /** Stream returning all the messages starting from the given offset, from all
    * the configured partitions.
    *
    * @param offsets Starting position for all the partitions
    *
    * @return A source of IoT messages
    */
  def source(offsets: OffsetList): JavaSource[MessageFromDevice, NotUsed] = {
    new JavaSource(iotHub.source(OffsetListScalaDSL(offsets)))
  }

  /** Stream returning all the messages starting from the given offset, from all
    * the configured partitions.
    *
    * @param offsets    Starting position for all the partitions
    * @param partitions Partitions to process
    *
    * @return A source of IoT messages
    */
  def source(offsets: OffsetList, partitions: PartitionList): JavaSource[MessageFromDevice, NotUsed] = {
    new JavaSource(iotHub.source(OffsetListScalaDSL(offsets), PartitionListScalaDSL(partitions)))
  }

  /** Stream returning all the messages starting from the given time, from all
    * the configured partitions.
    *
    * @param startTime       Starting position expressed in time
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    *
    * @return A source of IoT messages
    */
  def source(startTime: Instant, withCheckpoints: java.lang.Boolean): JavaSource[MessageFromDevice, NotUsed] = {
    new JavaSource(iotHub.source(startTime, withCheckpoints))
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
  def source(startTime: Instant, withCheckpoints: java.lang.Boolean, partitions: PartitionList): JavaSource[MessageFromDevice, NotUsed] = {
    new JavaSource(iotHub.source(startTime, withCheckpoints, PartitionListScalaDSL(partitions)))
  }

  /** Stream returning all the messages starting from the given offset, from all
    * the configured partitions.
    *
    * @param offsets         Starting position for all the partitions
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    *
    * @return A source of IoT messages
    */
  def source(offsets: OffsetList, withCheckpoints: java.lang.Boolean): JavaSource[MessageFromDevice, NotUsed] = {
    new JavaSource(iotHub.source(OffsetListScalaDSL(offsets), withCheckpoints))
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
  def source(offsets: OffsetList, withCheckpoints: java.lang.Boolean, partitions: PartitionList): JavaSource[MessageFromDevice, NotUsed] = {
    new JavaSource(iotHub.source(OffsetListScalaDSL(offsets), withCheckpoints, PartitionListScalaDSL(partitions)))
  }
}
