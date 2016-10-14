// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.test.helpers

import com.microsoft.azure.iothub._

/* Test helper to send messages to the hub */
class Device(deviceId: String) extends Logger {

  private[this] class EventCallback extends IotHubEventCallback {
    override def execute(status: IotHubStatusCode, context: scala.Any): Unit = {
      val i = context.asInstanceOf[Int]
      log.debug(s"Message ${i} status ${status.name()}")
    }
  }

  // Load device credentials
  private[this] lazy val credentials = Configuration.deviceCredentials(deviceId)

  // Prepare connection string for this device
  private[this] lazy val connString = DeviceConnectionString.build(
    Configuration.iotHubName, credentials.deviceId, credentials.primaryKey)

  // Prepare client to send messages
  private[this] lazy val client = new DeviceClient(connString, IotHubClientProtocol.AMQPS)

  def disconnect(): Unit = {
    client.close()
  }

  def sendMessage(text: String, sequenceNumber: Int): Unit = {
    client.open()
    log.debug(s"Device '$deviceId' sending '$text'")
    val message = new Message(text)
    client.sendEventAsync(message, new EventCallback(), sequenceNumber)
  }
}
