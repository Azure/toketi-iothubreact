// Copyright (c) Microsoft. All rights reserved.

// NOTE: Namespace chosen to avoid access to internal classes
package api

// NOTE: No global imports to make easier detecting breaking changes

class APIIsBackwardCompatible extends org.scalatest.FeatureSpec with org.scalatest.mockito.MockitoSugar {

  info("As a developer using Azure IoT hub React")
  info("I want to be able to upgrade to new minor versions without changing my code")
  info("So I can benefit from improvements without excessive development costs")

  implicit val cpconfig = mock[com.microsoft.azure.iot.iothubreact.checkpointing.ICPConfiguration]

  feature("Version 0.x is backward compatible") {

    scenario("Using MessageFromDevice") {
      import com.microsoft.azure.iot.iothubreact.MessageFromDevice

      val data: Option[com.microsoft.azure.eventhubs.EventData] = None
      val partition: Option[Int] = Some(1)

      // Test properties
      val partitionInfo = Some(new com.microsoft.azure.eventhubs.ReceiverRuntimeInformation(partition.toString))
      val message1 = new MessageFromDevice(data, partition, partitionInfo)
      lazy val properties: java.util.Map[String, String] = message1.properties
      lazy val isKeepAlive: Boolean = message1.isKeepAlive
      lazy val messageSchema: String = message1.messageSchema
      lazy val contentType: String = message1.contentType
      lazy val created: java.time.Instant = message1.received
      lazy val offset: String = message1.offset
      lazy val sequenceNumber: Long = message1.sequenceNumber
      lazy val deviceId: String = message1.deviceId
      lazy val messageId: String = message1.messageId
      lazy val content: Array[Byte] = message1.content
      lazy val contentAsString: String = message1.contentAsString
      assert(message1.isKeepAlive == false)

      // Named parameters
      val message2: MessageFromDevice = new MessageFromDevice(data = data, partNumber = partition, partInfo = partitionInfo)

      // Keepalive
      val message3: MessageFromDevice = new MessageFromDevice(data, partNumber = None, partInfo = None)
      assert(message3.isKeepAlive == true)
    }

    scenario("Using Scala DSL OffsetList") {
      import com.microsoft.azure.iot.iothubreact.scaladsl.OffsetList

      val o1: String = "123"
      val o2: String = "foo"

      // Ctors
      val offset1: OffsetList = OffsetList(Seq(o1, o2))
      val offset2: OffsetList = new OffsetList(Seq(o1, o2))

      // Named parameters
      val offset3: OffsetList = OffsetList(values = Seq(o1, o2))
      val offset4: OffsetList = new OffsetList(values = Seq(o1, o2))

      assert(offset1.values(0) == o1)
      assert(offset1.values(1) == o2)
      assert(offset2.values(0) == o1)
      assert(offset2.values(1) == o2)
      assert(offset3.values(0) == o1)
      assert(offset3.values(1) == o2)
      assert(offset4.values(0) == o1)
      assert(offset4.values(1) == o2)
    }

    scenario("Using Java DSL OffsetList") {
      import com.microsoft.azure.iot.iothubreact.javadsl.OffsetList

      val o1: String = "123"
      val o2: String = "foo"

      // Ctors
      val offset1: OffsetList = new OffsetList(java.util.Arrays.asList(o1, o2))

      // Named parameters
      val offset2: OffsetList = new OffsetList(values = java.util.Arrays.asList(o1, o2))

      assert(offset1.values.get(0) == o1)
      assert(offset1.values.get(1) == o2)
      assert(offset2.values.get(0) == o1)
      assert(offset2.values.get(1) == o2)
    }

    scenario("Using Scala DSL PartitionList") {
      import com.microsoft.azure.iot.iothubreact.scaladsl.PartitionList

      val o1: Int = 1
      val o2: Int = 5

      // Ctors
      val offset1: PartitionList = PartitionList(Seq(o1, o2))
      val offset2: PartitionList = new PartitionList(Seq(o1, o2))

      // Named parameters
      val offset3: PartitionList = PartitionList(values = Seq(o1, o2))
      val offset4: PartitionList = new PartitionList(values = Seq(o1, o2))

      assert(offset1.values(0) == o1)
      assert(offset1.values(1) == o2)
      assert(offset2.values(0) == o1)
      assert(offset2.values(1) == o2)
      assert(offset3.values(0) == o1)
      assert(offset3.values(1) == o2)
      assert(offset4.values(0) == o1)
      assert(offset4.values(1) == o2)
    }

    scenario("Using Java DSL PartitionList") {
      import com.microsoft.azure.iot.iothubreact.javadsl.PartitionList

      val o1: Int = 1
      val o2: Int = 5

      // Ctors
      val offset1: PartitionList = new PartitionList(java.util.Arrays.asList(o1, o2))

      // Named parameters
      val offset2: PartitionList = new PartitionList(values = java.util.Arrays.asList(o1, o2))

      assert(offset1.values.get(0) == o1)
      assert(offset1.values.get(1) == o2)
      assert(offset2.values.get(0) == o1)
      assert(offset2.values.get(1) == o2)
    }

    scenario("Using ResumeOnError") {
      import akka.actor.ActorSystem
      import akka.stream.ActorMaterializer
      import com.microsoft.azure.iot.iothubreact.ResumeOnError._

      val as: ActorSystem = actorSystem
      val mat: ActorMaterializer = materializer
    }

    scenario("Using StopOnError") {
      import akka.actor.ActorSystem
      import akka.stream.ActorMaterializer
      import com.microsoft.azure.iot.iothubreact.StopOnError._

      val as: ActorSystem = actorSystem
      val mat: ActorMaterializer = materializer
    }

    scenario("Using CheckpointBackend") {
      import com.microsoft.azure.iot.iothubreact.checkpointing.backends.CheckpointBackend

      class CustomBackend extends CheckpointBackend {

        override def readOffset(partition: Int): String = {
          return ""
        }

        override def writeOffset(partition: Int, offset: String): Unit = {}
      }

      val backend: CustomBackend = new CustomBackend()

      val anyname = java.util.UUID.randomUUID.toString
      org.mockito.Mockito.when(cpconfig.storageNamespace).thenReturn(anyname)
      assert(backend.checkpointNamespace == anyname)
    }

    scenario("Using Message Type") {
      import com.microsoft.azure.iot.iothubreact.MessageFromDevice
      import com.microsoft.azure.iot.iothubreact.filters.MessageSchema

      val filter1: (MessageFromDevice) ⇒ Boolean = MessageSchema("some")
      val filter2: MessageSchema = new MessageSchema("some")
    }

    scenario("Using Scala DSL IoTHub") {
      import java.time.Instant

      import akka.NotUsed
      import akka.stream.scaladsl.Source
      import com.microsoft.azure.iot.iothubreact.MessageFromDevice
      import com.microsoft.azure.iot.iothubreact.scaladsl.{IoTHub, OffsetList, PartitionList}

      val hub1: IoTHub = new IoTHub()
      val hub2: IoTHub = IoTHub()

      val offsets: OffsetList = OffsetList(Seq("1", "0", "0", "-1", "234623"))
      val partitions: PartitionList = PartitionList(Seq(0, 1, 3))

      var source: Source[MessageFromDevice, NotUsed] = hub1.source()

      source = hub1.source(partitions)
      source = hub1.source(partitions = partitions)

      source = hub1.source(Instant.now())
      source = hub1.source(startTime = Instant.now())

      source = hub1.source(Instant.now(), partitions)
      source = hub1.source(startTime = Instant.now(), partitions = partitions)

      source = hub1.source(false)
      source = hub1.source(withCheckpoints = false)

      source = hub1.source(false, partitions)
      source = hub1.source(withCheckpoints = false, partitions = partitions)

      source = hub1.source(offsets)
      source = hub1.source(offsets = offsets)

      source = hub1.source(offsets, partitions)
      source = hub1.source(offsets = offsets, partitions = partitions)

      source = hub1.source(Instant.now(), false)
      source = hub1.source(startTime = Instant.now(), withCheckpoints = false)

      source = hub1.source(Instant.now(), false, partitions)
      source = hub1.source(startTime = Instant.now(), withCheckpoints = false, partitions = partitions)

      source = hub1.source(offsets, false)
      source = hub1.source(offsets = offsets, withCheckpoints = false)

      source = hub1.source(offsets, false, partitions)
      source = hub1.source(offsets = offsets, withCheckpoints = false, partitions = partitions)

      hub1.close()
      hub2.close()
    }

    scenario("Using Java DSL IoTHub") {
      import java.time.Instant

      import akka.NotUsed
      import akka.stream.javadsl.Source
      import com.microsoft.azure.iot.iothubreact.MessageFromDevice
      import com.microsoft.azure.iot.iothubreact.javadsl.{IoTHub, OffsetList, PartitionList}

      val hub: IoTHub = new IoTHub()

      val offsets: OffsetList = new OffsetList(java.util.Arrays.asList("1", "0", "0", "0", "-1", "234623"))
      val partitions: PartitionList = new PartitionList(java.util.Arrays.asList(0, 1, 3))

      var source: Source[MessageFromDevice, NotUsed] = hub.source()

      source = hub.source(partitions)
      source = hub.source(partitions = partitions)

      source = hub.source(Instant.now())
      source = hub.source(startTime = Instant.now())

      source = hub.source(Instant.now(), partitions)
      source = hub.source(startTime = Instant.now(), partitions = partitions)

      source = hub.source(false)
      source = hub.source(withCheckpoints = false)

      source = hub.source(false, partitions)
      source = hub.source(withCheckpoints = false, partitions = partitions)

      source = hub.source(offsets)
      source = hub.source(offsets = offsets)

      source = hub.source(offsets, partitions)
      source = hub.source(offsets = offsets, partitions = partitions)

      source = hub.source(Instant.now(), false)
      source = hub.source(startTime = Instant.now(), withCheckpoints = false)

      source = hub.source(Instant.now(), false, partitions)
      source = hub.source(startTime = Instant.now(), withCheckpoints = false, partitions = partitions)

      source = hub.source(offsets, false)
      source = hub.source(offsets = offsets, withCheckpoints = false)

      source = hub.source(offsets, false, partitions)
      source = hub.source(offsets = offsets, withCheckpoints = false, partitions = partitions)

      hub.close()
    }
  }
}
