package com.microsoft.azure.iot.iothubreact.checkpointing

import akka.actor.{ActorSystem, Props}
import com.microsoft.azure.iot.iothubreact.checkpointing.CheckpointService.{ReadCheckpoint, CheckpointInMemory, CheckpointToStorage}
import com.microsoft.azure.iot.iothubreact.checkpointing.backends.CheckpointBackend
import com.microsoft.azure.iot.iothubreact.config.{IConfiguration, IConnectConfiguration}
import org.scalatest.{FeatureSpec, GivenWhenThen}
import org.scalatest.Matchers._
import org.scalatest.mockito.MockitoSugar
import org.mockito.Matchers._
import org.mockito.Mockito._

import scala.concurrent.duration._
import scala.language.postfixOps
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{Await, Future}

class CheckpointServiceTest extends FeatureSpec with GivenWhenThen with MockitoSugar {

  Feature("CheckpointService writes offsets") {

    Scenario("Writer is hashed") {

      case class CustomBackend() extends CheckpointBackend {

        case class Checkpoint(endpoint: String, partition: Int, offset: String)

        var writes = Seq[Checkpoint]()

        override def readOffset(endpoint: String, partition: Int): String = {
          return writes.filter{x ⇒ x.endpoint == endpoint && x.partition == partition}.lastOption.map{_.offset}.getOrElse("-1")
        }

        override def writeOffset(endpoint: String, partition: Int, offset: String): Unit = {
          writes = writes :+ Checkpoint(endpoint, partition, offset)
        }
      }

      val backend: CustomBackend = new CustomBackend()

      val partition = 0
      val config = mock[IConfiguration]
      val cpconfig = mock[ICPConfiguration]

      //not mocked to take advantage of hashing
      val cconfig = new IConnectConfiguration {val iotHubNamespace : String = "sb://iothub-ns-mything-pm1-987654-a3be9917ba.servicebus.windows.net/"
                                               val iotHubName      : String = ""
                                               val iotHubPartitions: Int                        = 1
                                               val accessConnString: String = ""
                                               val accessPolicy    : String = ""
                                               val accessKey       : String = ""
      }
      val locator = mock[ICheckpointServiceLocator]
      when(locator.getCheckpointBackend(any())).thenReturn(backend)
      when(config.checkpointing).thenReturn(cpconfig)
      when(config.connect).thenReturn(cconfig)
      when(cpconfig.checkpointFrequency).thenReturn(1 millisecond)
      when(cpconfig.checkpointTimeThreshold).thenReturn(1 millisecond)
      when(cpconfig.checkpointCountThreshold).thenReturn(0)

      val actorSystem = ActorSystem("testing")

      val actor = actorSystem.actorOf(Props(new CheckpointService(config, partition, locator)), "testing")

      backend.writes.size should be(0)

      actor ! CheckpointInMemory("10")
      actor ! CheckpointInMemory("100")

      backend.writes.size should be(0)

      actor ! CheckpointToStorage

      //wait for asynch
      Thread.sleep(100)

      backend.writes.size should be(1)
      backend.writes.last.offset should be("100")
      backend.writes.last.endpoint should be("-VCZvQfOmbgoy_hWO_IZVg==")

      //set up a second actor using the same backend and query it
      implicit val to: Timeout = Timeout(10 seconds)
      val another = actorSystem.actorOf(Props(new CheckpointService(config, partition, locator)), "testing2")
      Await.result((another ? ReadCheckpoint).mapTo[String], 10 seconds) should be("100")
    }
  }

}
