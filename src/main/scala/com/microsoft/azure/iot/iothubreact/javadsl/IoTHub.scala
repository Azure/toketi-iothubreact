// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.javadsl

import java.time.Instant
import java.util.concurrent.CompletionStage

import akka.stream.javadsl.{Sink, Source ⇒ JavaSource}
import akka.{Done, NotUsed}
import com.microsoft.azure.iot.iothubreact._
import com.microsoft.azure.iot.iothubreact.checkpointing.{IOffsetLoader, OffsetLoader}
import com.microsoft.azure.iot.iothubreact.config.{Configuration, IConfiguration}
import com.microsoft.azure.iot.iothubreact.scaladsl.{IoTHub ⇒ IoTHubScalaDSL}
import com.microsoft.azure.iot.iothubreact.sinks.{DevicePropertiesSink, MessageToDeviceSink, MethodOnDeviceSink, OffsetSaveSink}

/** Provides a streaming source to retrieve messages from Azure IoT Hub
  */
class IoTHub(config: IConfiguration, offsetLoader: IOffsetLoader) {

  // Parameterless ctor
  def this() = this(Configuration(), new OffsetLoader(Configuration()))

  private lazy val iotHub = IoTHubScalaDSL(config)

  /** Stop the stream
    */
  def close(): Unit = iotHub.close()

  /** Sink to send asynchronous messages to IoT devices
    *
    * @return Streaming sink
    */
  def messageSink: Sink[MessageToDevice, CompletionStage[Done]] =
    MessageToDeviceSink().javaSink()

  /**
    * Provides an offset sink that can be incorporated into a graph for at-least-once semantics
    */
  def offsetSaveSink(): Sink[MessageFromDevice, CompletionStage[Done]] =
    OffsetSaveSink(config, offsetLoader).javaSink()

  /** Sink to call synchronous methods on IoT devices
    *
    * TODO: make public when implemented
    *
    * @return Streaming sink
    */
  private[this] def methodSink: Sink[MethodOnDevice, CompletionStage[Done]] =
    MethodOnDeviceSink().javaSink()

  /** Sink to asynchronously set properties on IoT devices
    *
    * TODO: make public when implemented
    *
    * @return Streaming sink
    */
  private[this] def propertySink: Sink[DeviceProperties, CompletionStage[Done]] =
    DevicePropertiesSink().javaSink()

  /** Stream returning all the messages since the beginning, from all the
    * configured partitions.
    *
    * @return A source of IoT messages
    */
  def source(): JavaSource[MessageFromDevice, NotUsed] = new JavaSource(iotHub.source())

  /** Stream returning all the messages from all the requested partitions.
    * If checkpointing the stream starts from the last position saved, otherwise
    * it starts from the beginning.
    *
    * @param partitions Partitions to process
    *
    * @return A source of IoT messages
    */
  def source(partitions: java.util.List[java.lang.Integer]): JavaSource[MessageFromDevice, NotUsed] = {
    new JavaSource(iotHub.source(SourceOptions().partitions(partitions)))
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

  /** Stream events using the requested options
    *
    * @param options Set of streaming options
    *
    * @return A source of IoT messages
    */
  def source(options: SourceOptions): JavaSource[MessageFromDevice, NotUsed] = {
    new JavaSource(iotHub.source(options))
  }
}
