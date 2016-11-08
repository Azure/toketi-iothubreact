// Copyright (c) Microsoft. All rights reserved.

import java.util.UUID

import com.microsoft.azure.iothub._

class Device(hubName: String, deviceId: String, accessKey: String, verbose: Boolean) {

  private val connString = ConnectionString.build(hubName, deviceId, accessKey)
  private val modelProp  = "$$contentModel"
  private val formatProp = "$$contentType"
  private var ready      = true
  private val waitOnSend = 5000
  private val waitUnit   = 100

  if (verbose) println("Connecting...")
  private val client = new DeviceClient(connString, IotHubClientProtocol.HTTPS)
  client.open()

  private class EventCallback extends IotHubEventCallback {
    override def execute(status: IotHubStatusCode, context: scala.Any): Unit = {
      ready = true
      if (verbose) println("Message sent.")
    }
  }

  def isReady: Boolean = ready

  def sendMessage(content: String, format: String, model: String): Unit = {
    try {
      if (!ready) {
        throw new RuntimeException("The device client is busy")
      }

      ready = false

      // Prepare message
      val message = new Message(content)
      message.setCorrelationId(UUID.randomUUID().toString)
      if (format.nonEmpty) message.setProperty(formatProp, format)
      if (model.nonEmpty) message.setProperty(modelProp, model)

      // Send
      if (verbose) println(s"Sending message '${content}' ...")
      client.sendEventAsync(message, new EventCallback(), None)

      // Wait a bit
      if (verbose) println("Waiting for confirmation...")
      var wait = waitOnSend
      if (!ready) while (wait > 0 && !ready) {
        Thread.sleep(waitUnit)
        wait -= waitUnit
      }
    } catch {
      case e: Exception => {
        ready = false
        client.close()
        throw e
      }
    }
  }

  def disconnect(): Unit = {
    if (verbose) println("Disconnecting...")
    ready = false
    client.close()
    if (verbose) println(s"Disconnected.")
  }
}

