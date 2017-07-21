// Copyright (c) Microsoft. All rights reserved.

package it.helpers

import java.nio.file.{Files, Paths}

import com.microsoft.azure.eventhubs.EventHubClient
import com.typesafe.config.{Config, ConfigFactory}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.reflect.io.File

/* Test configuration settings */
object Configuration {

  // JSON parser setup, brings in default date formats etc.
  implicit val formats = DefaultFormats

  private[this] val confConnPath      = "iothub-react.connection."
  private[this] val confStreamingPath = "iothub-react.streaming."

  private[this] val conf: Config = ConfigFactory.load()

  // Read-only settings
  val iotHubNamespace : String = getNamespaceFromEndpoint(conf.getString(confConnPath + "hubEndpoint"))
  val iotHubName      : String = conf.getString(confConnPath + "hubName")
  val iotHubPartitions: Int    = conf.getInt(confConnPath + "hubPartitions")
  val accessConnString: String = conf.getString(confConnPath + "accessConnString")
  val accessPolicy    : String = getAccessPolicy(accessConnString)
  val accessKey       : String = getAccessKey(accessConnString)

  // Tests can override these
  var receiverConsumerGroup: String = EventHubClient.DEFAULT_CONSUMER_GROUP_NAME
  var receiverTimeout      : Long   = conf.getDuration(confStreamingPath + "receiverTimeout").toMillis
  var receiverBatchSize    : Int    = conf.getInt(confStreamingPath + "receiverBatchSize")

  //  // Read devices configuration from JSON file
  //  private[this] lazy val devicesJsonFile                       = conf.getString(confConnPath + "devices")
  //  private[this] lazy val devicesJson: String                   = File(devicesJsonFile).slurp()
  //  private[this] lazy val devices    : Array[DeviceCredentials] = parse(devicesJson).extract[Array[DeviceCredentials]]

  //  def deviceCredentials(id: String): DeviceCredentials = {
  //    val deviceData: Option[DeviceCredentials] = devices.find(x ⇒ x.deviceId == id)
  //    if (deviceData == None) {
  //      throw new RuntimeException(s"Device '${id}' credentials not found")
  //    }
  //    deviceData.get
  //  }

  private[this] def getNamespaceFromEndpoint(endpoint: String): String = {
    endpoint.replaceFirst(".*://", "").replaceFirst("\\..*", "")
  }

  private[this] def getAccessPolicy(text: String): String = {
    """.*SharedAccessKeyName=([^;]*).*""".r
      .findFirstMatchIn(text)
      .map(_ group 1)
      .getOrElse("SharedAccessKeyNameNotFound")
  }

  private[this] def getAccessKey(text: String): String = {
    """.*SharedAccessKey=([^;]*).*""".r
      .findFirstMatchIn(text)
      .map(_ group 1)
      .getOrElse("SharedAccessKeyNotFound")
  }
}
