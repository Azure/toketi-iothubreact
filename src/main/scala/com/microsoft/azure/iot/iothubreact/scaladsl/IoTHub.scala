// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.scaladsl

import java.time.Instant

import akka.stream._
import akka.stream.scaladsl._
import akka.{Done, NotUsed}
import com.microsoft.azure.iot.iothubreact._
import com.microsoft.azure.iot.iothubreact.checkpointing.{Configuration ⇒ CPConfiguration}

import scala.concurrent.Future
import scala.language.postfixOps

/** Provides a streaming source to retrieve messages from Azure IoT Hub
  */
case class IoTHub() extends Logger {

  // TODO: Provide ClearCheckpoints() method to clear the state

  private[this] val streamManager = new StreamManager[MessageFromDevice]

  private[this] def allPartitions = Some(PartitionList(0 until Configuration.iotHubPartitions))

  private[this] def fromStart =
    Some(OffsetList(List.fill[String](Configuration.iotHubPartitions)(IoTHubPartition.OffsetStartOfStream)))

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
  def sink[A]()(implicit typedSink: TypedSink[A]): Sink[A, Future[Done]] = typedSink.definition

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
      withCheckpoints = withCheckpoints && CPConfiguration.isEnabled)
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
      withCheckpoints = withCheckpoints && CPConfiguration.isEnabled)
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
      withCheckpoints = withCheckpoints && CPConfiguration.isEnabled)
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
      withCheckpoints = withCheckpoints && CPConfiguration.isEnabled)
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
      withCheckpoints = withCheckpoints && CPConfiguration.isEnabled)
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
      withCheckpoints = withCheckpoints && CPConfiguration.isEnabled)
  }

  /** Stream returning all the messages, from the given starting point, optionally with
    * checkpointing
    *
    * @param partitions      Partitions to process
    * @param offsets         Starting positions using the offset property in the messages
    * @param startTime       Starting position expressed in time
    * @param withTimeOffset  Whether the start point is a timestamp
    * @param withCheckpoints Whether to read/write the stream position
    *
    * @return A source of IoT messages
    */
  private[this] def getSource(
      partitions: Option[PartitionList] = None,
      offsets: Option[OffsetList] = None,
      startTime: Instant = Instant.MIN,
      withTimeOffset: Boolean = false,
      withCheckpoints: Boolean = true): Source[MessageFromDevice, NotUsed] = {

    val graph = GraphDSL.create() {
      implicit b ⇒
        import GraphDSL.Implicits._

        val merge = b.add(Merge[MessageFromDevice](partitions.get.values.size))

        for (partition ← partitions.get.values) {
          val graph = if (withTimeOffset)
            IoTHubPartition(partition).source(startTime, withCheckpoints).via(streamManager)
          else
            IoTHubPartition(partition).source(offsets.get.values(partition), withCheckpoints).via(streamManager)

          val source = Source.fromGraph(graph).async
          source ~> merge
        }

        SourceShape(merge.out)
    }

    Source.fromGraph(graph)
  }
}
