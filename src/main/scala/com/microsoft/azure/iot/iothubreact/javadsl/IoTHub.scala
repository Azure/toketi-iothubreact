// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.javadsl

import java.time.Instant

import akka.NotUsed
import akka.stream.javadsl.{Source ⇒ SourceJavaDSL}
import com.microsoft.azure.eventhubs.PartitionReceiver
import com.microsoft.azure.iot.iothubreact.IoTMessage
import com.microsoft.azure.iot.iothubreact.scaladsl.{IoTHub ⇒ IoTHubScalaDSL}

/** Provides a streaming source to retrieve messages from Azure IoT Hub
  *
  * @todo Support reading the same partition from multiple clients
  */
class IoTHub() extends {

  // Offset used to start reading from the beginning
  val OffsetStartOfStream: String = PartitionReceiver.START_OF_STREAM

  private lazy val iotHub = new IoTHubScalaDSL()

  /** Stream returning all the messages since the beginning, from the specified
    * partition.
    *
    * @param partition IoT hub partition number (0-based). The number of
    *                  partitions is set during the deployment.
    *
    * @return A source of IoT messages
    */
  def source(partition: Int): SourceJavaDSL[IoTMessage, NotUsed] = {
    new SourceJavaDSL(iotHub.source(partition))
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
  def source(partition: Int, offset: String): SourceJavaDSL[IoTMessage, NotUsed] = {
    new SourceJavaDSL(iotHub.source(partition, offset))
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
  def source(partition: Int, startTime: Instant): SourceJavaDSL[IoTMessage, NotUsed] = {
    new SourceJavaDSL(iotHub.source(partition, startTime))
  }

  /** Stream returning all the messages since the beginning, from all the
    * configured partitions.
    *
    * @return A source of IoT messages
    */
  def source(): SourceJavaDSL[IoTMessage, NotUsed] = {
    new SourceJavaDSL(iotHub.source())
  }

  /** Stream returning all the messages starting from the given offset, from all
    * the configured partitions.
    *
    * @param offset Starting position
    *
    * @return A source of IoT messages
    */
  def source(offset: String): SourceJavaDSL[IoTMessage, NotUsed] = {
    new SourceJavaDSL(iotHub.source(offset))
  }

  /** Stream returning all the messages starting from the given time, from all
    * the configured partitions.
    *
    * @param startTime Starting position expressed in time
    *
    * @return A source of IoT messages
    */
  def source(startTime: Instant): SourceJavaDSL[IoTMessage, NotUsed] = {
    new SourceJavaDSL(iotHub.source(startTime))
  }
}
