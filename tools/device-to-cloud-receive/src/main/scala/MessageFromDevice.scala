// Copyright (c) Microsoft. All rights reserved.

package messagesFromDevices

import java.time.Instant
import java.util

import com.microsoft.azure.eventhubs.EventData

/* MessageFromDevice factory */
object MessageFromDevice {
  def apply(rawData: EventData, partition: Option[Int]): MessageFromDevice = {
    new MessageFromDevice(Some(rawData), partition)
  }
}

class MessageFromDevice(data: Option[EventData], val partition: Option[Int]) {

  private val contentTypeProperty = "$$contentType"
  private val modelProperty       = "$$contentModel"

  private[this] lazy val systemProps = data.get.getSystemProperties()

  // Note: empty when using MQTT
  lazy val properties: util.Map[String, String] = data.get.getProperties()

  // Note: MQTT not supported yet
  lazy val model: String = properties.getOrDefault(modelProperty, "")

  // @todo Make public once available in Azure SDK
  private lazy val contentType = properties.getOrDefault(contentTypeProperty, "")

  lazy val created: Instant = systemProps.getEnqueuedTime

  lazy val offset: String = systemProps.getOffset

  lazy val sequenceNumber: Long = systemProps.getSequenceNumber

  lazy val deviceId: String = systemProps.get("iothub-connection-device-id").toString

  lazy val content: Array[Byte] = data.get.getBody

  lazy val contentAsString: String = new String(content)
}
