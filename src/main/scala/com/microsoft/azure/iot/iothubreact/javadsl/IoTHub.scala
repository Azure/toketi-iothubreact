// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.javadsl

import java.time.Instant

import akka.NotUsed
import akka.stream.javadsl.{Source ⇒ SourceJavaDSL}
import com.microsoft.azure.iot.iothubreact.IoTMessage
import com.microsoft.azure.iot.iothubreact.scaladsl.{IoTHub ⇒ IoTHubScalaDSL}

import scala.collection.JavaConverters._

/** Provides a streaming source to retrieve messages from Azure IoT Hub
  *
  * @todo (*) Provide ClearCheckpoints() method to clear the state
  */
class IoTHub() {

  private lazy val iotHub = new IoTHubScalaDSL()

  /** Stream returning all the messages since the beginning, from all the
    * configured partitions.
    *
    * @return A source of IoT messages
    */
  def source(): SourceJavaDSL[IoTMessage, NotUsed] = {
    new SourceJavaDSL(iotHub.source())
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

  /** Stream returning all the messages from all the configured partitions.
    * If checkpointing the stream starts from the last position saved, otherwise
    * it starts from the beginning.
    *
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    *
    * @return A source of IoT messages
    */
  def source(withCheckpoints: Boolean): SourceJavaDSL[IoTMessage, NotUsed] = {
    new SourceJavaDSL(iotHub.source(withCheckpoints))
  }

  /** Stream returning all the messages starting from the given offset, from all
    * the configured partitions.
    *
    * @param offsets Starting position for all the partitions
    *
    * @return A source of IoT messages
    */
  def source(offsets: java.util.Collection[String]): SourceJavaDSL[IoTMessage, NotUsed] = {
    new SourceJavaDSL(iotHub.source(offsets.asScala.toList))
  }

  /** Stream returning all the messages starting from the given time, from all
    * the configured partitions.
    *
    * @param startTime       Starting position expressed in time
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    *
    * @return A source of IoT messages
    */
  def source(startTime: Instant, withCheckpoints: Boolean): SourceJavaDSL[IoTMessage, NotUsed] = {
    new SourceJavaDSL(iotHub.source(startTime, withCheckpoints))
  }

  /** Stream returning all the messages starting from the given offset, from all
    * the configured partitions.
    *
    * @param offsets         Starting position for all the partitions
    * @param withCheckpoints Whether to read/write the stream position (default: true)
    *
    * @return A source of IoT messages
    */
  def source(offsets: java.util.Collection[String], withCheckpoints: Boolean): SourceJavaDSL[IoTMessage, NotUsed] = {
    new SourceJavaDSL(iotHub.source(offsets.asScala.toList, withCheckpoints))
  }
}
