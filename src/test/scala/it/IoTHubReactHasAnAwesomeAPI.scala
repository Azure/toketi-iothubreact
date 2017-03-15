// Copyright (c) Microsoft. All rights reserved.

package it

import java.time.Instant

import akka.NotUsed
import akka.stream.scaladsl.{Sink, Source}
import com.microsoft.azure.iot.iothubreact.MessageFromDevice
import com.microsoft.azure.iot.iothubreact.scaladsl.{IoTHub, IoTHubPartition}
import org.scalatest._

class IoTHubReactHasAnAwesomeAPI extends FeatureSpec with GivenWhenThen {

  info("As a client of Azure IoT hub")
  info("I want to be able to receive device messages as a stream")
  info("So I can process them asynchronously and at scale")

  feature("IoT Hub React has an awesome API") {

    scenario("Developer wants to retrieve IoT messages") {

      Given("An IoT hub is configured")
      val hub = IoTHub()

      When("A developer wants to fetch messages from Azure IoT hub")
      val messagesFromAllPartitions: Source[MessageFromDevice, NotUsed] = hub.source(false)
      val messagesFromNowOn: Source[MessageFromDevice, NotUsed] = hub.source(Instant.now(), false)

      Then("The messages are presented as a stream")
      messagesFromAllPartitions.to(Sink.ignore)
      messagesFromNowOn.to(Sink.ignore)

      hub.close()
    }
  }
}
