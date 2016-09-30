// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.scaladsl

import java.time.Instant

import akka.NotUsed
import akka.stream.SourceShape
import akka.stream.scaladsl._
import com.microsoft.azure.eventhubs.PartitionReceiver
import com.microsoft.azure.iot.iothubreact._

/** Provides a streaming source to retrieve messages from Azure IoT Hub
  *
  * @todo Support reading the same partition from multiple clients
  */
class IoTHub {

  // Offset used to start reading from the beginning
  val OffsetStartOfStream: String = PartitionReceiver.START_OF_STREAM

  /** Stream returning all the messages since the beginning, from the specified
    * partition.
    *
    * @param partition IoT hub partition number (0-based). The number of
    *                  partitions is set during the deployment.
    *
    * @return A source of IoT messages
    */
  def source(partition: Int): Source[IoTMessage, NotUsed] = {
    IoTMessageSource(partition, OffsetStartOfStream)
  }

  /** Stream returning all the messages from the given offset, from the
    * specified partition.
    *
    * @param partition IoT hub partition number (0-based). The number of
    *                  partitions is set during the deployment.
    * @param offset    Starting position, offset of the first message
    *
    * @return A source of IoT messages
    */
  def source(partition: Int, offset: String): Source[IoTMessage, NotUsed] = {
    IoTMessageSource(partition, offset)
  }

  /** Stream returning all the messages from the given offset, from the
    * specified partition.
    *
    * @param partition IoT hub partition number (0-based). The number of
    *                  partitions is set during the deployment.
    * @param startTime Starting position expressed in time
    *
    * @return A source of IoT messages
    */
  def source(partition: Int, startTime: Instant): Source[IoTMessage, NotUsed] = {
    IoTMessageSource(partition, startTime)
  }

  /** Stream returning all the messages since the beginning, from all the
    * configured partitions.
    *
    * @return A source of IoT messages
    */
  def source(): Source[IoTMessage, NotUsed] = source(OffsetStartOfStream)

  /** Stream returning all the messages starting from the given offset, from all
    * the configured partitions.
    *
    * @param offset Starting position
    *
    * @return A source of IoT messages
    */
  def source(offset: String): Source[IoTMessage, NotUsed] = {
    source(offset, Instant.MIN, false)
  }

  /** Stream returning all the messages starting from the given time, from all
    * the configured partitions.
    *
    * @param startTime Starting position expressed in time
    *
    * @return A source of IoT messages
    */
  def source(startTime: Instant): Source[IoTMessage, NotUsed] = {
    source("", startTime, true)
  }

  /** Stream returning all the messages, from the given starting point
    *
    * @param offset     Starting position using the offset property in the messages
    * @param startTime  Starting position expressed in time
    * @param timeOffset Whether the start point is a timestamp
    *
    * @return A source of IoT messages
    */
  private[this] def source(
      offset: String,
      startTime: Instant,
      timeOffset: Boolean): Source[IoTMessage, NotUsed] = {

    val graph = GraphDSL.create() {
      implicit b ⇒
        import GraphDSL.Implicits._

        val merge = b.add(Merge[IoTMessage](Configuration.iotHubPartitions))

        for (p ← 0 until Configuration.iotHubPartitions) {
          val graph = if (timeOffset) IoTMessageSource(p, startTime) else IoTMessageSource(p, offset)
          val source = Source.fromGraph(graph).async
          source ~> merge
        }

        SourceShape(merge.out)
    }

    Source.fromGraph(graph)
  }
}
