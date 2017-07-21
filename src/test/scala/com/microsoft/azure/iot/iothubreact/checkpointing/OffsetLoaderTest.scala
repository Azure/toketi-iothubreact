package com.microsoft.azure.iot.iothubreact.checkpointing

import com.microsoft.azure.iot.iothubreact.config.{IConfiguration, IConnectConfiguration}
import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito.when
import org.scalatest.Matchers._

class OffsetLoaderTest extends FunSuite with MockitoSugar {

  test("test GetSavedOffsets handles None appropriately") {

    val config = mock[IConfiguration]
    val cnConfig = mock[IConnectConfiguration]
    when(config.connect) thenReturn (cnConfig)
    when(cnConfig.iotHubPartitions) thenReturn (10)
    val loader = StubbedLoader(config)
    loader.GetSavedOffsets should be(Map(0 → "Offset 0", 1 → "Offset 1", 3 → "Offset 3"))
  }

  case class StubbedLoader(config: IConfiguration) extends OffsetLoader(config) {

    override private[iothubreact] def GetSavedOffset(partition: Int) = {
      partition match {
        case 0 ⇒ Some("Offset 0")
        case 1 ⇒ Some("Offset 1")
        case 3 ⇒ Some("Offset 3")
        case _ ⇒ None
      }
    }
  }
}
