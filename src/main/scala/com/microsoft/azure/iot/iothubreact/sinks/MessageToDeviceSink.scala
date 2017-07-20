// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.sinks

import java.util.concurrent.CompletionStage

import akka.Done
import akka.japi.function.Procedure
import akka.stream.javadsl.{Sink ⇒ JavaSink}
import akka.stream.scaladsl.{Sink ⇒ ScalaSink}
import com.microsoft.azure.iot.iothubreact.config.{ConnectConfiguration, IConnectConfiguration}
import com.microsoft.azure.iot.iothubreact.{Logger, MessageFromDevice, MessageToDevice}
import com.microsoft.azure.sdk.iot.service.{IotHubServiceClientProtocol, ServiceClient}

object MessageToDeviceSink {
  def apply(): MessageToDeviceSink = new MessageToDeviceSink()

  def apply(config: IConnectConfiguration): MessageToDeviceSink = new MessageToDeviceSink(config)
}

/** Send commands (asynchronous messages) from cloud to devices
  */
class MessageToDeviceSink(config: IConnectConfiguration)
  extends ISink[MessageToDevice]
    with Logger {

  // Parameterless ctor
  def this() = this(ConnectConfiguration())

  private[iothubreact] val protocol     = IotHubServiceClientProtocol.AMQPS
  private[iothubreact] val timeoutMsecs = 15000

  private[this] val connString    = s"HostName=${config.accessHostname};" +
    s"SharedAccessKeyName=${config.accessPolicy};" +
    s"SharedAccessKey=${config.accessKey}"
  private[this] val serviceClient = ServiceClient.createFromConnectionString(connString, protocol)

  log.info("Connecting client to ${} ...", config.accessHostname)
  serviceClient.open()

  def scalaSink(): ScalaSink[MessageToDevice, scala.concurrent.Future[Done]] =
    ScalaSink.foreach[MessageToDevice](
      m ⇒ {
        log.info("Sending message to device {}", m.deviceId)
        serviceClient.sendAsync(m.deviceId, m.message)
      })

  def javaSink(): JavaSink[MessageToDevice, CompletionStage[Done]] =
    JavaSink.foreach[MessageToDevice] {
      JavaSinkProcedure
    }

  // Required for Scala 2.11
  private[this] object JavaSinkProcedure extends Procedure[MessageToDevice] {
    @scala.throws[Exception](classOf[Exception])
    override def apply(m: MessageToDevice): Unit = {
      log.info("Sending message to device " + m.deviceId)
      serviceClient.sendAsync(m.deviceId, m.message)
    }
  }
}
