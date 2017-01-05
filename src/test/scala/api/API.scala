// Copyright (c) Microsoft. All rights reserved.

// Namespace chosen to avoid access to internal classes
package api

// No global imports to make easier detecting breaking changes
import org.scalatest.FeatureSpec

class APIIsBackwardCompatible extends FeatureSpec {

  info("As a developer using Azure IoT hub React")
  info("I want to be able to upgrade to new minor versions without changing my code")
  info("So I can benefit from improvements without excessive development costs")

  feature("Version 0.x is backward compatible") {

    scenario("Using MessageFromDevice") {
      import com.microsoft.azure.iot.iothubreact.MessageFromDevice

      val data: Option[com.microsoft.azure.eventhubs.EventData] = None
      val partition: Option[Int] = Some(1)

      // Test properties
      val message1 = new MessageFromDevice(data, partition)
      lazy val properties: java.util.Map[String, String] = message1.properties
      lazy val isKeepAlive: Boolean = message1.isKeepAlive
      lazy val messageType: String = message1.messageType
      lazy val contentType: String = message1.contentType
      lazy val created: java.time.Instant = message1.created
      lazy val offset: String = message1.offset
      lazy val sequenceNumber: Long = message1.sequenceNumber
      lazy val deviceId: String = message1.deviceId
      lazy val messageId: String = message1.messageId
      lazy val content: Array[Byte] = message1.content
      lazy val contentAsString: String = message1.contentAsString
      assert(message1.isKeepAlive == false)

      // Named parameters
      val message2: MessageFromDevice = new MessageFromDevice(data = data, partition = partition)

      // Keepalive
      val message3: MessageFromDevice = new MessageFromDevice(data, None)
      assert(message3.isKeepAlive == true)
    }

    scenario("Using Offset") {
      import com.microsoft.azure.iot.iothubreact.Offset

      val offset: String = "123"

      // Ctors
      val offset1: Offset = Offset(offset)
      val offset2: Offset = new Offset(offset)

      // Named parameters
      val offset3: Offset = Offset(value = offset)
      val offset4: Offset = new Offset(value = offset)

      assert(offset1.value == offset)
      assert(offset2.value == offset)
      assert(offset3.value == offset)
      assert(offset4.value == offset)
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
      assert(backend.checkpointNamespace == "iothub-react-checkpoints")
    }

    scenario("Using Message Type") {
      import com.microsoft.azure.iot.iothubreact.MessageFromDevice
      import com.microsoft.azure.iot.iothubreact.filters.MessageType

      val filter1: (MessageFromDevice) ⇒ Boolean = MessageType("some")
      val filter2: MessageType = new MessageType("some")
    }

    scenario("Using ScalaDSL IoTHub") {
      import java.time.Instant

      import akka.NotUsed
      import akka.stream.scaladsl.Source
      import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHub
      import com.microsoft.azure.iot.iothubreact.{MessageFromDevice, Offset}

      val hub1: IoTHub = new IoTHub()
      val hub2: IoTHub = IoTHub()

      val source1: Source[MessageFromDevice, NotUsed] = hub1.source()

      val source2: Source[MessageFromDevice, NotUsed] = hub1.source(Instant.now())
      val source3: Source[MessageFromDevice, NotUsed] = hub1.source(startTime = Instant.now())

      val source4: Source[MessageFromDevice, NotUsed] = hub1.source(false)
      val source5: Source[MessageFromDevice, NotUsed] = hub1.source(withCheckpoints = false)

      val offsets: Seq[Offset] = Vector(Offset("1"), Offset("0"), Offset("0"), Offset("-1"), Offset("234623"))
      val source6: Source[MessageFromDevice, NotUsed] = hub1.source(offsets)
      val source7: Source[MessageFromDevice, NotUsed] = hub1.source(offsets = offsets)

      val source8: Source[MessageFromDevice, NotUsed] = hub1.source(Instant.now(), false)
      val source9: Source[MessageFromDevice, NotUsed] = hub1.source(startTime = Instant.now(), withCheckpoints = false)

      val source10: Source[MessageFromDevice, NotUsed] = hub1.source(offsets, false)
      val source11: Source[MessageFromDevice, NotUsed] = hub1.source(offsets = offsets, withCheckpoints = false)

      hub1.close()
      hub2.close()
    }

    scenario("Using JavaDSL IoTHub") {
      import java.time.Instant

      import akka.NotUsed
      import akka.stream.javadsl.Source
      import com.microsoft.azure.iot.iothubreact.javadsl.IoTHub
      import com.microsoft.azure.iot.iothubreact.{MessageFromDevice, Offset}

      import scala.collection.JavaConverters._

      val hub: IoTHub = new IoTHub()

      val source1: Source[MessageFromDevice, NotUsed] = hub.source()

      val source2: Source[MessageFromDevice, NotUsed] = hub.source(Instant.now())
      val source3: Source[MessageFromDevice, NotUsed] = hub.source(startTime = Instant.now())

      val source4: Source[MessageFromDevice, NotUsed] = hub.source(false)
      val source5: Source[MessageFromDevice, NotUsed] = hub.source(withCheckpoints = false)

      val offsets: java.util.Collection[Offset] = Seq(Offset("1"), Offset("0"), Offset("0"), Offset("-1"), Offset("234623")).asJavaCollection
      val source6: Source[MessageFromDevice, NotUsed] = hub.source(offsets)
      val source7: Source[MessageFromDevice, NotUsed] = hub.source(offsets = offsets)

      val source8: Source[MessageFromDevice, NotUsed] = hub.source(Instant.now(), false)
      val source9: Source[MessageFromDevice, NotUsed] = hub.source(startTime = Instant.now(), withCheckpoints = false)

      val source10: Source[MessageFromDevice, NotUsed] = hub.source(offsets, false)
      val source11: Source[MessageFromDevice, NotUsed] = hub.source(offsets = offsets, withCheckpoints = false)

      hub.close()
    }

    scenario("Using ScalaDSL IoTHubPartition") {
      import java.time.Instant

      import akka.NotUsed
      import akka.stream.scaladsl.Source
      import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHubPartition
      import com.microsoft.azure.iot.iothubreact.{MessageFromDevice, Offset}

      val hub1: IoTHubPartition = new IoTHubPartition(1)
      val hub2: IoTHubPartition = new IoTHubPartition(partition = 1)
      val hub3: IoTHubPartition = IoTHubPartition(1)
      val hub4: IoTHubPartition = IoTHubPartition(partition = 1)

      val const1: String = IoTHubPartition.OffsetStartOfStream
      val const2: String = IoTHubPartition.OffsetCheckpointNotFound

      val source1: Source[MessageFromDevice, NotUsed] = hub1.source()

      val source2: Source[MessageFromDevice, NotUsed] = hub1.source(Instant.now())
      val source3: Source[MessageFromDevice, NotUsed] = hub1.source(startTime = Instant.now())

      val source4: Source[MessageFromDevice, NotUsed] = hub1.source(false)
      val source5: Source[MessageFromDevice, NotUsed] = hub1.source(withCheckpoints = false)

      val source6: Source[MessageFromDevice, NotUsed] = hub1.source(Offset("1"))
      val source7: Source[MessageFromDevice, NotUsed] = hub1.source(offset = Offset("1"))

      val source8: Source[MessageFromDevice, NotUsed] = hub1.source(Instant.now(), false)
      val source9: Source[MessageFromDevice, NotUsed] = hub1.source(startTime = Instant.now(), withCheckpoints = false)

      val source10: Source[MessageFromDevice, NotUsed] = hub1.source(Offset("1"), false)
      val source11: Source[MessageFromDevice, NotUsed] = hub1.source(offset = Offset("1"), withCheckpoints = false)

      hub1.close()
      hub2.close()
      hub3.close()
      hub4.close()
    }

    scenario("Using JavaDSL IoTHubPartition") {
      import java.time.Instant

      import akka.NotUsed
      import akka.stream.javadsl.Source
      import com.microsoft.azure.iot.iothubreact.javadsl.IoTHubPartition
      import com.microsoft.azure.iot.iothubreact.{MessageFromDevice, Offset}

      val hub1: IoTHubPartition = new IoTHubPartition(1)
      val hub2: IoTHubPartition = new IoTHubPartition(partition = 1)

      val const1: String = hub1.OffsetStartOfStream
      val const2: String = hub1.OffsetCheckpointNotFound

      val source1: Source[MessageFromDevice, NotUsed] = hub1.source()

      val source2: Source[MessageFromDevice, NotUsed] = hub1.source(Instant.now())
      val source3: Source[MessageFromDevice, NotUsed] = hub1.source(startTime = Instant.now())

      val source4: Source[MessageFromDevice, NotUsed] = hub1.source(false)
      val source5: Source[MessageFromDevice, NotUsed] = hub1.source(withCheckpoints = false)

      val source6: Source[MessageFromDevice, NotUsed] = hub1.source(Offset("1"))
      val source7: Source[MessageFromDevice, NotUsed] = hub1.source(offset = Offset("1"))

      val source8: Source[MessageFromDevice, NotUsed] = hub1.source(Instant.now(), false)
      val source9: Source[MessageFromDevice, NotUsed] = hub1.source(startTime = Instant.now(), withCheckpoints = false)

      val source10: Source[MessageFromDevice, NotUsed] = hub1.source(Offset("1"), false)
      val source11: Source[MessageFromDevice, NotUsed] = hub1.source(offset = Offset("1"), withCheckpoints = false)

      hub1.close()
      hub2.close()
    }
  }
}
