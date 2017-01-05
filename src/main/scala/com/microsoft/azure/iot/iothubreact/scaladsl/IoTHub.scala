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
  *
  * @todo (*) Provide ClearCheckpoints() method to clear the state
  */
case class IoTHub() extends Logger {

  private[this] val streamManager = new StreamManager[MessageFromDevice]

  private[this] def fromStart =
    Some(List.fill[Offset](Configuration.iotHubPartitions)(Offset(IoTHubPartition.OffsetStartOfStream)))

  /** Stop the stream
    */
  def close(): Unit = {
    streamManager.close()
  }

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
  def source(offsets: Seq[Offset]): Source[MessageFromDevice, NotUsed] = {
    getSource(
      withTimeOffset = false,
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
  def source(offsets: Seq[Offset], withCheckpoints: Boolean): Source[MessageFromDevice, NotUsed] = {
    getSource(
      withTimeOffset = false,
      offsets = Some(offsets),
      withCheckpoints = withCheckpoints && CPConfiguration.isEnabled)
  }

  /** Stream returning all the messages, from the given starting point, optionally with
    * checkpointing
    *
    * @param offsets         Starting positions using the offset property in the messages
    * @param startTime       Starting position expressed in time
    * @param withTimeOffset  Whether the start point is a timestamp
    * @param withCheckpoints Whether to read/write the stream position
    *
    * @return A source of IoT messages
    */
  private[this] def getSource(
      offsets: Option[Seq[Offset]] = None,
      startTime: Instant = Instant.MIN,
      withTimeOffset: Boolean = false,
      withCheckpoints: Boolean = true): Source[MessageFromDevice, NotUsed] = {

    val graph = GraphDSL.create() {
      implicit b ⇒
        import GraphDSL.Implicits._

        val merge = b.add(Merge[MessageFromDevice](Configuration.iotHubPartitions))

        for (partition ← 0 until Configuration.iotHubPartitions) {
          val graph = if (withTimeOffset)
            IoTHubPartition(partition).source(startTime, withCheckpoints).via(streamManager)
          else
            IoTHubPartition(partition).source(offsets.get(partition), withCheckpoints).via(streamManager)

          val source = Source.fromGraph(graph).async
          source ~> merge
        }

        SourceShape(merge.out)
    }

    Source.fromGraph(graph)
  }
}
