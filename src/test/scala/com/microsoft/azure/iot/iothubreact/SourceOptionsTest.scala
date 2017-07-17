// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

import java.time.Instant

import com.microsoft.azure.iot.iothubreact.config.IConnectConfiguration
import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHubPartition
import org.mockito.Mockito._
import org.scalatest.FeatureSpec
import org.scalatest.mockito.MockitoSugar

class SourceOptionsTest extends FeatureSpec with MockitoSugar {

  info("As a developer streaming data from Azure IoT Hub")
  info("I can set streaming options using a flexible DSL")
  info("So that I can use all IoT Hub React features")

  Feature("The settings are consistent with user request") {

    val pcount = 6
    val conf = mock[IConnectConfiguration]
    when(conf.iotHubPartitions).thenReturn(pcount)

    Scenario("Default settings") {
      val o: SourceOptions = SourceOptions()
      assert(o.getStartTime.isEmpty)
      assert(o.getStartTimeOnNoCheckpoint.isEmpty)
      assert(o.getStartOffsets(conf).size == pcount)
      assert(o.getStartOffsets(conf).filter(x ⇒ x == IoTHubPartition.OffsetStartOfStream).size == pcount)
      assert(o.isFromStart)
      assert(!o.isSaveOffsetsOnPull)
      assert(!o.isFromSavedOffsets)
      assert(!o.isFromTime)
      assert(!o.isFromOffsets)
      assert(!o.isWithRuntimeInfo)
      assert(o.getPartitions(conf) == (0 until pcount))
    }

    Scenario("Selecting from which partitions") {
      val o: SourceOptions = SourceOptions().partitions(1, 3, 5)
      assert(o.getPartitions(conf) == Seq(1, 3, 5))
      assert(o.getStartOffsets(conf).size == pcount)
      assert(o.getStartOffsets(conf).filter(x ⇒ x == IoTHubPartition.OffsetStartOfStream).size == pcount)

      o.partitions(Seq(1, 2, 4))
      assert(o.getPartitions(conf) == Seq(1, 2, 4))

      o.allPartitions()
      assert(o.getPartitions(conf) == (0 until pcount))
      assert(o.getStartOffsets(conf).size == pcount)
      assert(o.getStartOffsets(conf).filter(x ⇒ x == IoTHubPartition.OffsetStartOfStream).size == pcount)
    }

    Scenario("Streaming from the start") {
      val o: SourceOptions = SourceOptions().fromStart
      assert(o.isFromStart)
      assert(!o.isFromSavedOffsets)
      assert(!o.isFromTime)
      assert(!o.isFromOffsets)
      assert(o.getStartOffsets(conf).size == pcount)
      assert(o.getStartOffsets(conf).filter(x ⇒ x == IoTHubPartition.OffsetStartOfStream).size == pcount)

      o.fromTime(Instant.now).fromStart
      assert(o.isFromStart)
      assert(!o.isFromSavedOffsets)
      assert(!o.isFromTime)
      assert(!o.isFromOffsets)
      assert(o.getStartOffsets(conf).size == pcount)
      assert(o.getStartOffsets(conf).filter(x ⇒ x == IoTHubPartition.OffsetStartOfStream).size == pcount)
    }

    Scenario("Streaming from some datetime") {
      val time = Instant.now.minusSeconds(1000)
      val o: SourceOptions = SourceOptions().fromTime(time)
      assert(o.isFromTime)
      assert(o.getStartTime.get == time)
      assert(!o.isFromStart)
      assert(!o.isFromSavedOffsets)
      assert(!o.isFromOffsets)

      val time2 = Instant.now.minusSeconds(2000)
      o.fromStart.fromTime(time2)
      assert(o.isFromTime)
      assert(o.getStartTime.get == time2)
      assert(!o.isFromStart)
      assert(!o.isFromSavedOffsets)
      assert(!o.isFromOffsets)
    }

    Scenario("Streaming from some offsets") {
      val o: SourceOptions = SourceOptions().fromOffsets("0", "400", "1", "2", "44", "1")
      assert(o.getStartOffsets(conf) == Seq("0", "400", "1", "2", "44", "1"))
      assert(o.isFromOffsets)
      assert(!o.isFromTime)
      assert(!o.isFromStart)
      assert(!o.isFromSavedOffsets)

      o.fromSavedOffsets().fromOffsets("20", "2400", "21", "22", "244", "210")
      assert(o.getStartOffsets(conf) == Seq("20", "2400", "21", "22", "244", "210"))
      assert(o.isFromOffsets)
      assert(!o.isFromTime)
      assert(!o.isFromStart)
      assert(!o.isFromSavedOffsets)
    }

    Scenario("Streaming from saved offsets") {
      val o: SourceOptions = SourceOptions().fromSavedOffsets()
      assert(o.getStartTimeOnNoCheckpoint.get == Instant.MIN)
      assert(o.isFromSavedOffsets)
      assert(!o.isSaveOffsetsOnPull)
      assert(!o.isFromOffsets)
      assert(!o.isFromTime)
      assert(!o.isFromStart)

      val time = Instant.now.minusSeconds(1000)
      o.fromStart.fromSavedOffsets(time)
      assert(o.getStartTimeOnNoCheckpoint.get == time)
      assert(o.isFromSavedOffsets)
      assert(!o.isSaveOffsetsOnPull)
      assert(!o.isFromOffsets)
      assert(!o.isFromTime)
      assert(!o.isFromStart)
    }

    Scenario("Save offsets while streaming") {
      val o: SourceOptions = SourceOptions()
      assert(!o.isSaveOffsetsOnPull)

      o.saveOffsetsOnPull()
      assert(o.isSaveOffsetsOnPull)
    }

    Scenario("Include runtime information in the stream") {
      val o: SourceOptions = SourceOptions()
      assert(!o.isWithRuntimeInfo)

      o.withRuntimeInfo()
      assert(o.isWithRuntimeInfo)
    }
  }
}
