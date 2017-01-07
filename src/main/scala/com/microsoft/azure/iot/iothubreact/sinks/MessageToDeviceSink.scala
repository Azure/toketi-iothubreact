// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.sinks

import java.util.concurrent.CompletionStage

import akka.Done
import akka.japi.function.Procedure
import akka.stream.javadsl.{Sink ⇒ JavaSink}
import akka.stream.scaladsl.{Sink ⇒ ScalaSink}
import com.microsoft.azure.iot.iothubreact.{Configuration, Logger, MessageToDevice}
import com.microsoft.azure.iot.service.sdk.{IotHubServiceClientProtocol, ServiceClient}

/** Send messages from cloud to devices
  */
case class MessageToDeviceSink() extends ISink[MessageToDevice] with Logger {

  private[iothubreact] val protocol     = IotHubServiceClientProtocol.AMQPS
  private[iothubreact] val timeoutMsecs = 15000

  private[this] val connString    = s"HostName=${Configuration.accessHostname};SharedAccessKeyName=${Configuration.accessPolicy};SharedAccessKey=${Configuration.accessKey}"
  private[this] val serviceClient = ServiceClient.createFromConnectionString(connString, protocol)

  private[this] object JavaSinkProcedure extends Procedure[MessageToDevice] {
    @scala.throws[Exception](classOf[Exception])
    override def apply(m: MessageToDevice): Unit = {
      log.info("Sending message to device " + m.deviceId)
      serviceClient.sendAsync(m.deviceId, m.message)
    }
  }

  log.info(s"Connecting client to ${Configuration.accessHostname} ...")
  serviceClient.open()

  def scalaSink(): ScalaSink[MessageToDevice, scala.concurrent.Future[Done]] =
    ScalaSink.foreach[MessageToDevice] {
      m ⇒ {
        log.info("Sending message to device " + m.deviceId)
        serviceClient.sendAsync(m.deviceId, m.message)
      }
    }

  def javaSink(): JavaSink[MessageToDevice, CompletionStage[Done]] =
    JavaSink.foreach[MessageToDevice] {
      JavaSinkProcedure
    }
}
