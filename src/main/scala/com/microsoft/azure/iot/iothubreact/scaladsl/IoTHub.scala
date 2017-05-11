// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.scaladsl

import java.time.Instant

import akka.stream._
import akka.stream.scaladsl._
import akka.{Done, NotUsed}
import com.microsoft.azure.iot.iothubreact._
import com.microsoft.azure.iot.iothubreact.checkpointing.{CPConfiguration, CheckpointService, ICPConfiguration}
import com.microsoft.azure.iot.iothubreact.sinks.{DevicePropertiesSink, MessageToDeviceSink, MethodOnDeviceSink, OffsetCommitSink}

import scala.concurrent.Future
import scala.language.postfixOps

/** Provides a streaming source to retrieve messages from Azure IoT Hub
  */
case class IoTHub(implicit config: ICPConfiguration = new CPConfiguration) extends Logger {

  // TODO: Provide ClearCheckpoints() method to clear the state

  private[this] val streamManager = new StreamManager

  private[this] def allPartitions = Some(PartitionList(0 until Configuration.iotHubPartitions))

  private[this] def fromStart =
    Some(OffsetList(List.fill[String](Configuration.iotHubPartitions)(IoTHubPartition.OffsetStartOfStream)))

  private lazy val commitSinkBackend = CheckpointService.configToBackend

  /** Stop the stream
    */
  def close(): Unit = {
    streamManager.close()
  }

  /** Sink to communicate with IoT devices
    *
    * @param typedSink Sink factory
    * @tparam A Type of communication (message, method, property)
    *
    * @return Streaming sink
    */
  def sink[A]()(implicit typedSink: TypedSink[A]): Sink[A, Future[Done]] = typedSink.scalaDefinition

  /** Sink to send asynchronous messages to IoT devices
    *
    * @return Streaming sink
    */
  def messageSink: Sink[MessageToDevice, Future[Done]] =
    MessageToDeviceSink().scalaSink()

  /** Sink to call synchronous methods on IoT devices
    *
    * @return Streaming sink
    */
  def methodSink: Sink[MethodOnDevice, Future[Done]] =
    MethodOnDeviceSink().scalaSink()

  /** Sink to asynchronously set properties on IoT devices
    *
    * @return Streaming sink
    */
  def propertySink: Sink[DeviceProperties, Future[Done]] =
    DevicePropertiesSink().scalaSink()

  /**
    * Provides an offset sink that can be incorporated into a graph for at-least-once semantics (withCheckpoints should be false)
    */
  def offsetSink(parallelism: Int): Sink[MessageFromDevice, Future[Done]] = OffsetCommitSink(parallelism, commitSinkBackend).scalaSink()

  /** Stream returning all the messages from all the configured partitions.
    * If checkpointing the stream starts from the last position saved, otherwise
    * it starts from the beginning.
    *
    * @return A source of IoT messages
    */
  def source(): Source[MessageFromDevice, NotUsed] = {
    getSource(
      withTimeOffset = false,
      partitions = allPartitions,
      offsets = fromStart,
      withCheckpoints = false)
  }

  /** Stream returning all the messages from all the requested partitions.
    * If checkpointing the stream starts from the last position saved, otherwise
    * it starts from the beginning.
    *
    * @param partitions Partitions to process
    *
    * @return A source of IoT messages
    */
  def source(partitions: PartitionList): Source[MessageFromDevice, NotUsed] = {
    getSource(
      withTimeOffset = false,
      partitions = Some(partitions),
      offsets = fromStart,
      withCheckpoints = false)
  }

  /** Stream returning all the messages starting from the given time, from all
    * the configured partitions.
    *
    * @param startTime Starting position expressed in time
    *
    * @return A source of IoT messages
    */
  def source(startTime: Instant): Source[MessageFromDevice, NotUsed] = {
    getSource(
      withTimeOffset = true,
      partitions = allPartitions,
      startTime = startTime,
      withCheckpoints = false)
  }

  /** Stream returning all the messages starting from the given time, from all
    * the requested partitions.
    *
    * @param startTime  Starting position expressed in time
    * @param partitions Partitions to process
    *
    * @return A source of IoT messages
    */
  def source(startTime: Instant, partitions: PartitionList): Source[MessageFromDevice, NotUsed] = {
    getSource(
      withTimeOffset = true,
      partitions = Some(partitions),
      startTime = startTime,
      withCheckpoints = false)
  }

  /** Stream returning all the messages from all the configured partitions.
    * If checkpointing the stream starts from the last position saved, otherwise
    * it starts from the beginning.
    *
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    *
    * @return A source of IoT messages
    */
  def source(withCheckpoints: Boolean): Source[MessageFromDevice, NotUsed] = {
    getSource(
      withTimeOffset = false,
      partitions = allPartitions,
      offsets = fromStart,
      withCheckpoints = withCheckpoints && config.isEnabled)
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
  def source(withCheckpoints: Boolean, partitions: PartitionList): Source[MessageFromDevice, NotUsed] = {
    getSource(
      withTimeOffset = false,
      partitions = Some(partitions),
      offsets = fromStart,
      withCheckpoints = withCheckpoints && config.isEnabled)
  }

  /** Stream returning all the messages starting from the given offset, from all
    * the configured partitions.
    *
    * @param offsets Starting position for all the partitions
    *
    * @return A source of IoT messages
    */
  def source(offsets: OffsetList): Source[MessageFromDevice, NotUsed] = {
    getSource(
      withTimeOffset = false,
      partitions = allPartitions,
      offsets = Some(offsets),
      withCheckpoints = false)
  }

  /** Stream returning all the messages starting from the given offset, from all
    * the configured partitions.
    *
    * @param offsets    Starting position for all the partitions
    * @param partitions Partitions to process
    *
    * @return A source of IoT messages
    */
  def source(offsets: OffsetList, partitions: PartitionList): Source[MessageFromDevice, NotUsed] = {
    getSource(
      withTimeOffset = false,
      partitions = Some(partitions),
      offsets = Some(offsets),
      withCheckpoints = false)
  }

  /** Stream returning all the messages starting from the given time, from all
    * the configured partitions.
    *
    * @param startTime       Starting position expressed in time
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    *
    * @return A source of IoT messages
    */
  def source(startTime: Instant, withCheckpoints: Boolean): Source[MessageFromDevice, NotUsed] = {
    getSource(
      withTimeOffset = true,
      partitions = allPartitions,
      startTime = startTime,
      withCheckpoints = withCheckpoints && config.isEnabled)
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
  def source(startTime: Instant, withCheckpoints: Boolean, partitions: PartitionList): Source[MessageFromDevice, NotUsed] = {
    getSource(
      withTimeOffset = true,
      partitions = Some(partitions),
      startTime = startTime,
      withCheckpoints = withCheckpoints && config.isEnabled)
  }

  /** Stream returning all the messages starting from the given offset, from all
    * the configured partitions.
    *
    * @param offsets         Starting position for all the partitions
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    *
    * @return A source of IoT messages
    */
  def source(offsets: OffsetList, withCheckpoints: Boolean): Source[MessageFromDevice, NotUsed] = {
    getSource(
      withTimeOffset = false,
      partitions = allPartitions,
      offsets = Some(offsets),
      withCheckpoints = withCheckpoints && config.isEnabled)
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
  def source(offsets: OffsetList, withCheckpoints: Boolean, partitions: PartitionList): Source[MessageFromDevice, NotUsed] = {
    getSource(
      withTimeOffset = false,
      partitions = Some(partitions),
      offsets = Some(offsets),
      withCheckpoints = withCheckpoints && config.isEnabled)
  }

  /** Stream returning all the messages from all the configured partitions.
    * The stream starts from the last position saved but does not save further positions automatically.
    * May be used in conjunction with the offsetCommitSink to provide At-Least-Once semantics
    *
    * @return A source of IoT messages
    */
  def sourceWithSavedCheckpoint(): Source[MessageFromDevice, NotUsed] = {
    getSource(
      withTimeOffset = false,
      partitions = allPartitions,
      offsets = fromStart,
      withCheckpoints = false,
      startFromSavedCheckpoint = true)
  }

  /** Stream returning all the messages, from the given starting point, optionally with
    * checkpointing
    *
    * @param partitions      Partitions to process
    * @param offsets         Starting positions using the offset property in the messages
    * @param startTime       Starting position expressed in time
    * @param withTimeOffset  Whether the start point is a timestamp
    * @param withCheckpoints Whether to read/write the stream position
    * @param startFromSavedCheckpoint Whether to read the stream position at start
    *
    * @return A source of IoT messages
    */
  private[this] def getSource(
      partitions: Option[PartitionList] = None,
      offsets: Option[OffsetList] = None,
      startTime: Instant = Instant.MIN,
      withTimeOffset: Boolean = false,
      withCheckpoints: Boolean = true,
      startFromSavedCheckpoint: Boolean = false): Source[MessageFromDevice, NotUsed] = {

    val graph = GraphDSL.create() {
      implicit b ⇒
        import GraphDSL.Implicits._

        val merge = b.add(Merge[MessageFromDevice](partitions.get.values.size))

        for (partition ← partitions.get.values) {
          val graph = (startFromSavedCheckpoint, withTimeOffset) match {
            case (false, false) => IoTHubPartition(partition).source(offsets.get.values(partition), withCheckpoints).via(streamManager)
            case (false, true) => IoTHubPartition(partition).source(startTime, withCheckpoints).via(streamManager)
            case (true, false) => IoTHubPartition(partition).sourceWithSavedCheckpoint(offsets.get.values(partition)).via(streamManager)
            case (true, true) => IoTHubPartition(partition).sourceWithSavedCheckpoint(startTime).via(streamManager)

          }

          val source = Source.fromGraph(graph).async
          source ~> merge
        }

        SourceShape(merge.out)
    }

    Source.fromGraph(graph)
  }
}
