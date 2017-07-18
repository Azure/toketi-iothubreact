// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

import java.time.Instant

import com.microsoft.azure.iot.iothubreact.config.IConnectConfiguration
import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHubPartition

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

object SourceOptions {
  def apply() = new SourceOptions
}

/** Source streaming options
  *
  * TODO: differentiate checkpoint type: automatic (concurrent) or manual (user at-least-once)
  */
class SourceOptions() {

  private[this] var _allPartitions          : Boolean             = true
  private[this] var _partitions             : Option[Seq[Int]]    = None
  private[this] var _isFromStart            : Boolean             = true
  private[this] var _isFromOffsets          : Boolean             = false
  private[this] var _isFromTime             : Boolean             = false
  private[this] var _isFromCheckpoint       : Boolean             = false
  private[this] var _startTime              : Option[Instant]     = None
  private[this] var _startTimeOnNoCheckpoint: Option[Instant]     = None
  private[this] var _startOffsets           : Option[Seq[String]] = None
  private[this] var _isSaveOffsetsOnPull    : Boolean             = false
  private[this] var _isWithRuntimeInfo      : Boolean             = false

  /** Set the options to retrieve events from all the Hub partitions
    *
    * @return Current instance
    */
  def allPartitions(): SourceOptions = {
    _allPartitions = true
    _partitions = None
    this
  }

  /** Define from which Hub partitions to retrieve events
    *
    * @param values List of partitions
    *
    * @return Current instance
    */
  def partitions[X: ClassTag](values: Int*): SourceOptions = {
    _partitions = Some(values)
    _allPartitions = false
    this
  }

  /** Define from which Hub partitions to retrieve events
    *
    * @param values List of partitions
    *
    * @return Current instance
    */
  def partitions(values: Seq[Int]): SourceOptions = partitions(values: _ *)

  /** Define from which Hub partitions to retrieve events
    *
    * @param values List of partitions
    *
    * @return Current instance
    */
  def partitions(values: java.util.List[java.lang.Integer]): SourceOptions = partitions(values.asScala.map(_.intValue()))

  /** Define from which Hub partitions to retrieve events
    *
    * @param values List of partitions
    *
    * @return Current instance
    */
  def partitions(values: Array[Int]): SourceOptions = partitions(values.toSeq)

  /** Set the options to retrieve events from the beginning of the stream
    *
    * @return Current instance
    */
  def fromStart(): SourceOptions = {
    _isFromStart = true
    _isFromOffsets = false
    _isFromTime = false
    _isFromCheckpoint = false
    _startTime = None
    _startTimeOnNoCheckpoint = None
    _startOffsets = None
    this
  }

  /** Set the options to retrieve events from a specific time
    *
    * @param value Start time
    *
    * @return Current instance
    */
  def fromTime(value: Instant): SourceOptions = {
    _startTime = Some(value)
    _isFromStart = false
    _isFromOffsets = false
    _isFromTime = true
    _isFromCheckpoint = false
    _startTimeOnNoCheckpoint = None
    _startOffsets = None
    this
  }

  /** Set the options to start streaming for the specified offsets
    *
    * @param values List of offsets
    *
    * @return Current instance
    */
  def fromOffsets[X: ClassTag](values: String*): SourceOptions = {
    _startOffsets = Some(values)
    _isFromStart = false
    _isFromOffsets = true
    _isFromTime = false
    _isFromCheckpoint = false
    _startTime = None
    _startTimeOnNoCheckpoint = None
    this
  }

  /** Set the options to start streaming for the specified offsets
    *
    * @param values List of offsets
    *
    * @return Current instance
    */
  def fromOffsets(values: Seq[String]): SourceOptions = fromOffsets(values: _ *)

  /** Set the options to start streaming for the specified offsets
    *
    * @param values List of offsets
    *
    * @return Current instance
    */
  def fromOffsets(values: java.util.List[java.lang.String]): SourceOptions = fromOffsets(values.asScala)

  /** Set the options to start streaming for the specified offsets
    *
    * @param values List of offsets
    *
    * @return Current instance
    */
  def fromOffsets(values: Array[java.lang.String]): SourceOptions = fromOffsets(values.toSeq)

  /** Set the options to start streaming for the saved offsets
    *
    * @param startTimeIfMissing
    *
    * @return Current instance
    */
  def fromSavedOffsets(startTimeIfMissing: Instant = Instant.MIN): SourceOptions = {
    _isFromStart = false
    _isFromOffsets = false
    _isFromTime = false
    _isFromCheckpoint = true
    _startOffsets = None
    _startTime = None
    _startTimeOnNoCheckpoint = Some(startTimeIfMissing)
    this
  }

  /** Set the options to store the stream offset
    *
    * @return Current instance
    */
  def saveOffsetsOnPull(): SourceOptions = {
    _isSaveOffsetsOnPull = true
    this
  }

  /** Set the options to include Hub runtime information in the stream
    *
    * @return Current instance
    */
  def withRuntimeInfo(): SourceOptions = {
    _isWithRuntimeInfo = true
    this
  }

  private[iothubreact] def getStartTime: Option[Instant] = _startTime

  private[iothubreact] def getStartTimeOnNoCheckpoint: Option[Instant] = _startTimeOnNoCheckpoint

  private[iothubreact] def getStartOffsets(config: IConnectConfiguration): Seq[String] = {
    if (!_isFromOffsets)
      List.fill[String](config.iotHubPartitions)(IoTHubPartition.OffsetStartOfStream)
    else {
      if (_startOffsets.get.size != config.iotHubPartitions)
        throw new RuntimeException(s"The number of stream offsets [${_startOffsets.get.size}] " +
          s"differs from the number of partitions [${config.iotHubPartitions}]")

      _startOffsets.get
    }
  }

  private[iothubreact] def getPartitions(config: IConnectConfiguration): Seq[Int] = {
    if (_allPartitions) 0 until config.iotHubPartitions
    else _partitions.get
  }

  private[iothubreact] def isFromStart: Boolean = _isFromStart

  private[iothubreact] def isFromOffsets: Boolean = _isFromOffsets

  private[iothubreact] def isFromTime: Boolean = _isFromTime

  private[iothubreact] def isFromSavedOffsets: Boolean = _isFromCheckpoint

  private[iothubreact] def isSaveOffsetsOnPull: Boolean = _isSaveOffsetsOnPull

  private[iothubreact] def isWithRuntimeInfo: Boolean = _isWithRuntimeInfo
}
