// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

import java.time.Instant

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import akka.stream.{Attributes, Outlet, SourceShape}
import com.microsoft.azure.eventhubs.{EventData, PartitionReceiver}

import scala.collection.JavaConverters._

private object IoTMessageSource {

  /** Create an instance of the messages source for the specified partition
    *
    * @param partition IoT hub partition to read
    * @param offset    Starting position, offset of the first message
    *
    * @return A source returning the body of the message sent from a device.
    *         Deserialization is left to the consumer.
    */
  def apply(partition: Int, offset: String): Source[IoTMessage, NotUsed] = {
    Source.fromGraph(new IoTMessageSource(partition, offset))
  }

  /** Create an instance of the messages source for the specified partition
    *
    * @param partition IoT hub partition to read
    * @param startTime Starting position expressed in time
    *
    * @return A source returning the body of the message sent from a device.
    *         Deserialization is left to the consumer.
    */
  def apply(partition: Int, startTime: Instant): Source[IoTMessage, NotUsed] = {
    Source.fromGraph(new IoTMessageSource(partition, startTime))
  }
}

/** Source of messages from one partition of the IoT hub storage
  *
  * @param partition Partition number (0 to N-1) to read from
  * @param offset    Starting position
  *
  * @todo Refactor and use async methods, compare performance
  * @todo Consider option to deserialize on the fly to [T], assuming JSON format
  */
private class IoTMessageSource(val partition: Int, val offset: String)
  extends GraphStage[SourceShape[IoTMessage]]
    with Logger {

  abstract class OffsetType

  case object SequenceOffset extends OffsetType

  case object TimeOffset extends OffsetType

  def this(partition: Int, startTime: Instant) {
    this(partition, "*not used*")
    offsetType = TimeOffset
    _startTime = startTime
  }

  // When retrieving messages, include the message with the initial offset
  private[this] val OFFSET_INCLUSIVE = true

  // Time of offset used when defining the start of the stream
  private[this] var offsetType: OffsetType = SequenceOffset

  private[this] var _startTime: Instant = Instant.MIN

  // Define the (sole) output port of this stage
  private[this] val out: Outlet[IoTMessage] = Outlet("IoTMessageSource")

  // Define the shape of this stage => SourceShape with the port defined above
  override val shape: SourceShape[IoTMessage] = SourceShape(out)

  // All state MUST be inside the GraphStageLogic, never inside the enclosing
  // GraphStage. This state is safe to read/write from all the callbacks
  // provided by GraphStageLogic and the registered handlers.
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    // Connect to the IoT hub storage
    lazy val receiver: PartitionReceiver = offsetType match {
      case SequenceOffset ⇒ {
        log.info(s"Connecting to partition ${partition.toString} starting from ${offset}")
        IoTHubStorage
          .createClient()
          .createReceiverSync(
            Configuration.receiverConsumerGroup,
            partition.toString,
            offset,
            OFFSET_INCLUSIVE)
      }
      case TimeOffset     ⇒ {
        log.info(s"Connecting to partition ${partition.toString} starting from ${_startTime}")
        IoTHubStorage
          .createClient()
          .createReceiverSync(
            Configuration.receiverConsumerGroup,
            partition.toString,
            _startTime)
      }
    }

    val emptyResult = new Array[IoTMessage](0).toList

    // @todo Consider pausing on empty partitions
    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        try {

          val messages: java.lang.Iterable[EventData] =
            receiver.receiveSync(Configuration.receiverBatchSize)

          if (messages == null) {
            log.debug(s"Partition ${partition} is empty")
            emitMultiple(out, emptyResult)
          } else {
            val iterator: scala.collection.immutable.Iterable[IoTMessage] =
              messages.asScala.map(e ⇒ IoTMessage(e, Some(partition))).toList
            emitMultiple(out, iterator)
          }
        } catch {
          case e: Exception ⇒ {
            log.error(e, "Fatal error: " + e.getMessage)
          }
        }
      }
    })
  }
}
