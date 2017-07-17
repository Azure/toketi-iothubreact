// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.scaladsl

import java.time.Instant

import akka.stream._
import akka.stream.scaladsl._
import akka.{Done, NotUsed}
import com.microsoft.azure.iot.iothubreact._
import com.microsoft.azure.iot.iothubreact.checkpointing.{CheckpointService, IOffsetLoader, OffsetLoader}
import com.microsoft.azure.iot.iothubreact.checkpointing.backends.CheckpointBackend
import com.microsoft.azure.iot.iothubreact.config.{Configuration, IConfiguration}
import com.microsoft.azure.iot.iothubreact.sinks.{DevicePropertiesSink, MessageToDeviceSink, MethodOnDeviceSink, OffsetSaveSink}

import scala.concurrent.Future
import scala.language.postfixOps

object IoTHub {
  def apply(): IoTHub = new IoTHub()

  def apply(config: IConfiguration): IoTHub = new IoTHub(config, new OffsetLoader(config))
}

/** Provides a streaming source to retrieve messages from Azure IoT Hub
  *
  * TODO: Provide ClearCheckpoints() method to clear the state
  */
class IoTHub(config: IConfiguration, offsetLoader: IOffsetLoader) extends Logger {

  // Parameterless ctor
  def this() = this(Configuration(), new OffsetLoader(Configuration()))

  private[this] val streamManager = new StreamManager

  private[this] def allPartitions = Some(0 until config.connect.iotHubPartitions)

  private[this] def fromStart = Some(List.fill[String](config.connect.iotHubPartitions)(IoTHubPartition.OffsetStartOfStream))

  private lazy val commitSinkBackend = CheckpointService.getCheckpointBackend(config.checkpointing)

  /** Stop the stream
    */
  def close(): Unit = streamManager.close()

  /** Sink to communicate with IoT devices
    *
    * @param typedSink Sink factory
    * @tparam A Type of communication (message, method, property)
    *
    * @return Streaming sink
    */
  def sink[A]()(implicit typedSink: TypedSink[A]): Sink[A, Future[Done]] = typedSink.scalaDefinition(config)

  /** Sink to send asynchronous messages to IoT devices
    *
    * @return Streaming sink
    */
  def messageSink: Sink[MessageToDevice, Future[Done]] =
    MessageToDeviceSink(config.connect).scalaSink()

  /** Sink to call synchronous methods on IoT devices
    *
    * @return Streaming sink
    */
  def methodSink: Sink[MethodOnDevice, Future[Done]] =
    MethodOnDeviceSink(config).scalaSink()

  /** Sink to asynchronously set properties on IoT devices
    *
    * @return Streaming sink
    */
  def propertySink: Sink[DeviceProperties, Future[Done]] =
    DevicePropertiesSink(config).scalaSink()

  /**
    * Provides an offset sink that can be incorporated into a graph for at-least-once semantics
    */
  def offsetSink(parallelism: Int)
    (implicit backend: CheckpointBackend = commitSinkBackend): Sink[MessageFromDevice, Future[Done]] =
    OffsetSaveSink(parallelism, backend, config, offsetLoader).scalaSink()

  /** Stream returning all the messages from all the configured partitions.
    * If checkpointing the stream starts from the last position saved, otherwise
    * it starts from the beginning.
    *
    * @return A source of IoT messages
    */
  def source(): Source[MessageFromDevice, NotUsed] = source(SourceOptions().allPartitions.fromStart)

  /** Stream returning all the messages from all the requested partitions.
    * If checkpointing the stream starts from the last position saved, otherwise
    * it starts from the beginning.
    *
    * @param partitions Partitions to process
    *
    * @return A source of IoT messages
    */
  def source(partitions: Seq[Int]): Source[MessageFromDevice, NotUsed] = source(SourceOptions().partitions(partitions))

  /** Stream returning all the messages starting from the given time, from all
    * the configured partitions.
    *
    * @param startTime Starting position expressed in time
    *
    * @return A source of IoT messages
    */
  def source(startTime: Instant): Source[MessageFromDevice, NotUsed] = source(SourceOptions().fromTime(startTime))

  /** Stream returning all the messages, from the given starting point, optionally with
    * checkpointing
    *
    * @return A source of IoT messages
    */
  def source(options: SourceOptions): Source[MessageFromDevice, NotUsed] = {

    val partitions: Seq[Int] = options.getPartitions(config.connect)

    val graph = GraphDSL.create() {
      implicit b ⇒
        import GraphDSL.Implicits._

        val merge = b.add(Merge[MessageFromDevice](partitions.size))

        for (partition ← partitions) {
          val graph = IoTHubPartition(config, offsetLoader, partition).source(options).via(streamManager)
          val source = Source.fromGraph(graph).async
          source ~> merge
        }

        SourceShape(merge.out)
    }

    Source.fromGraph(graph)
  }
}
