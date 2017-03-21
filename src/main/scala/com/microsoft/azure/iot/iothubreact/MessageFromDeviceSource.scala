// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

import java.time.Instant

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import akka.stream.{Attributes, Outlet, SourceShape}
import com.microsoft.azure.eventhubs.{PartitionReceiver, ReceiverOptions, ReceiverRuntimeInformation}

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}

private object MessageFromDeviceSource {

  /** Create an instance of the messages source for the specified partition
    *
    * @param partition IoT hub partition to read
    * @param offset    Starting position, offset of the first message
    *
    * @return A source returning the body of the message sent from a device.
    *         Deserialization is left to the consumer.
    */
  def apply(config: IConfiguration, partition: Int, offset: String, withCheckpoints: Boolean): Source[MessageFromDevice, NotUsed] = {
    Source.fromGraph(new MessageFromDeviceSource(config, partition, offset, withCheckpoints))
  }

  /** Create an instance of the messages source for the specified partition
    *
    * @param partition IoT hub partition to read
    * @param startTime Starting position expressed in time
    *
    * @return A source returning the body of the message sent from a device.
    *         Deserialization is left to the consumer.
    */
  def apply(config: IConfiguration, partition: Int, startTime: Instant, withCheckpoints: Boolean): Source[MessageFromDevice, NotUsed] = {
    Source.fromGraph(new MessageFromDeviceSource(config, partition, startTime, withCheckpoints))
  }
}

/** Source of messages from one partition of the IoT hub storage
  */
private class MessageFromDeviceSource(config: IConfiguration) extends GraphStage[SourceShape[MessageFromDevice]] with Logger {

  // TODO: Refactor and use async methods, compare performance
  // TODO: Consider option to deserialize on the fly to [T], when JSON Content Type

  abstract class OffsetType

  case object UnspecifiedOffset extends OffsetType

  case object SequenceOffset extends OffsetType

  case object TimeOffset extends OffsetType

  // When retrieving messages, include the message with the initial offset
  private[this] val OffsetInclusive = true

  private[this] var _partition      : Option[Int]     = None
  private[this] var _offset         : Option[String]  = None
  private[this] var _withCheckpoints: Option[Boolean] = None
  private[this] var _startTime      : Option[Instant] = None
  private[this] var offsetType      : OffsetType      = UnspecifiedOffset

  private[this] def partition: Int = _partition.get

  private[this] def offset: String = _offset.get

  private[this] def withCheckpoints: Boolean = _withCheckpoints.get

  private[this] def startTime: Instant = _startTime.get

  /** Source of messages from one partition of the IoT hub storage
    *
    * @param partition       Partition number (0 to N-1) to read from
    * @param offset          Starting position
    * @param withCheckpoints Whether to read/write current position
    */
  def this(config: IConfiguration, partition: Int, offset: String, withCheckpoints: Boolean) {
    this(config)
    _partition = Some(partition)
    _offset = Some(offset)
    _withCheckpoints = Some(withCheckpoints)
    offsetType = SequenceOffset
  }

  /** Source of messages from one partition of the IoT hub storage
    *
    * @param partition       Partition number (0 to N-1) to read from
    * @param startTime       Starting position
    * @param withCheckpoints Whether to read/write current position
    */
  def this(config: IConfiguration, partition: Int, startTime: Instant, withCheckpoints: Boolean) {
    this(config)
    _partition = Some(partition)
    _startTime = Some(startTime)
    _withCheckpoints = Some(withCheckpoints)
    offsetType = TimeOffset
  }

  // Define the (sole) output port of this stage
  private[this] val out: Outlet[MessageFromDevice] = Outlet("MessageFromDeviceSource")

  // Define the shape of this stage ⇒ SourceShape with the port defined above
  override val shape: SourceShape[MessageFromDevice] = SourceShape(out)

  // All state MUST be inside the GraphStageLogic, never inside the enclosing
  // GraphStage. This state is safe to read/write from all the callbacks
  // provided by GraphStageLogic and the registered handlers.
  override def createLogic(attr: Attributes): GraphStageLogic = {
    log.debug(s"Creating the IoT hub source")
    new GraphStageLogic(shape) {

      val keepAliveSignal = new MessageFromDevice(None, None, None)
      val emptyResult     = List[MessageFromDevice](keepAliveSignal)

      lazy val receiver = getIoTHubReceiver()

      setHandler(
        out, new OutHandler {
          log.debug(s"Defining the output handler")

          override def onPull(): Unit = {
            try {
              val messages = Retry(2, 1 seconds) {
                receiver.receiveSync(config.receiverBatchSize)
              }

              if (messages == null) {
                log.debug(s"Partition ${partition} is empty")
                emitMultiple(out, emptyResult)
              } else {
                val partitionInfo: ReceiverRuntimeInformation = receiver.getRuntimeInformation
                val iterator = messages.asScala.map(e ⇒ MessageFromDevice(e, Some(partition), Some(partitionInfo))).toList
                log.debug(s"Emitting ${iterator.size} messages")
                emitMultiple(out, iterator)
              }
            } catch {
              case e: Exception ⇒ log.error(e, "Fatal error: " + e.getMessage)
            }
          }

          override def onDownstreamFinish(): Unit = {
            super.onDownstreamFinish()
            log.info(s"Closing partition ${partition} receiver")
            receiver.closeSync()
          }
        })

      /** Connect to the IoT hub storage
        *
        * @return IoT hub storage receiver
        */
      def getIoTHubReceiver(): PartitionReceiver = Retry(3, 2 seconds) {
        val receiverOptions = new ReceiverOptions()
        receiverOptions.setReceiverRuntimeMetricEnabled(config.retrieveRuntimeMetric)
        offsetType match {

          case SequenceOffset ⇒
            log.info(s"Connecting to partition ${partition.toString} starting from offset '${offset}'")
            IoTHubStorage(config)
              .createClient()
              .createReceiverSync(
                config.receiverConsumerGroup,
                partition.toString,
                offset,
                OffsetInclusive,
                receiverOptions)

          case TimeOffset ⇒
            log.info(s"Connecting to partition ${partition.toString} starting from time '${startTime}'")
            IoTHubStorage(config)
              .createClient()
              .createReceiverSync(
                config.receiverConsumerGroup,
                partition.toString,
                startTime,
                receiverOptions)
        }
      }
    }
  }
}
