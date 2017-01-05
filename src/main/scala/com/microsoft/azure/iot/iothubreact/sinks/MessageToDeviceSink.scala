// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.sinks

import akka.Done
import akka.stream.scaladsl.Sink
import com.microsoft.azure.iot.iothubreact.{Configuration, Logger, MessageToDevice}
import com.microsoft.azure.iot.service.sdk.{IotHubServiceClientProtocol, ServiceClient}

import scala.concurrent.Future

/** Send messages from cloud to devices
  */
case class MessageToDeviceSink() extends Logger {

  private[iothubreact] val protocol     = IotHubServiceClientProtocol.AMQPS
  private[iothubreact] val timeoutMsecs = 15000

  private[this] val connString    = s"HostName=${Configuration.accessHostname};SharedAccessKeyName=${Configuration.accessPolicy};SharedAccessKey=${Configuration.accessKey}"
  private[this] val serviceClient = ServiceClient.createFromConnectionString(connString, protocol)

  log.info(s"Connecting client to ${Configuration.accessHostname} ...")
  serviceClient.open()

  def sink(): Sink[MessageToDevice, Future[Done]] = Sink.foreach[MessageToDevice] {
    m ⇒ {
      log.info("Sending message to device " + m.deviceId)
      serviceClient.sendAsync(m.deviceId, m.message)
    }
  }
}
