// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.scaladsl

import java.time.Instant

import akka.NotUsed
import akka.stream.SourceShape
import akka.stream.scaladsl._
import com.microsoft.azure.iot.iothubreact._
import com.microsoft.azure.iot.iothubreact.checkpointing.{Configuration ⇒ CPConfiguration}

import scala.language.postfixOps

object IoTHub {
  def apply(): IoTHub = new IoTHub
}

/** Provides a streaming source to retrieve messages from Azure IoT Hub
  *
  * @todo (*) Provide ClearCheckpoints() method to clear the state
  */
class IoTHub extends Logger {

  private[this] def fromStart =
    Some(List.fill[Offset](Configuration.iotHubPartitions)(Offset(IoTHubPartition.OffsetStartOfStream)))

  /** Stream returning all the messages from all the configured partitions.
    * If checkpointing the stream starts from the last position saved, otherwise
    * it starts from the beginning.
    *
    * @return A source of IoT messages
    */
  def source(): Source[IoTMessage, NotUsed] = {
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
  def source(startTime: Instant): Source[IoTMessage, NotUsed] = {
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
  def source(withCheckpoints: Boolean): Source[IoTMessage, NotUsed] = {
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
  def source(offsets: List[Offset]): Source[IoTMessage, NotUsed] = {
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
  def source(startTime: Instant, withCheckpoints: Boolean): Source[IoTMessage, NotUsed] = {
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
  def source(offsets: List[Offset], withCheckpoints: Boolean): Source[IoTMessage, NotUsed] = {
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
      offsets: Option[List[Offset]] = None,
      startTime: Instant = Instant.MIN,
      withTimeOffset: Boolean = false,
      withCheckpoints: Boolean = true): Source[IoTMessage, NotUsed] = {

    val graph = GraphDSL.create() {
      implicit b ⇒
        import GraphDSL.Implicits._

        val merge = b.add(Merge[IoTMessage](Configuration.iotHubPartitions))

        for (partition ← 0 until Configuration.iotHubPartitions) {
          val graph = if (withTimeOffset)
            IoTHubPartition(partition).source(startTime, withCheckpoints)
          else
            IoTHubPartition(partition).source(offsets.get(partition), withCheckpoints)

          val source = Source.fromGraph(graph).async
          source ~> merge
        }

        SourceShape(merge.out)
    }

    Source.fromGraph(graph)
  }
}
