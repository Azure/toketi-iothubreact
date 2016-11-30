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
  val iotHubNamespace : String = conf.getString(confConnPath + "namespace")
  val iotHubName      : String = conf.getString(confConnPath + "name")
  val iotHubPartitions: Int    = conf.getInt(confConnPath + "partitions")
  val accessPolicy    : String = conf.getString(confConnPath + "accessPolicy")
  val accessKey       : String = conf.getString(confConnPath + "accessKey")

  // Tests can override these
  var receiverConsumerGroup: String = EventHubClient.DEFAULT_CONSUMER_GROUP_NAME
  var receiverTimeout      : Long   = conf.getDuration(confStreamingPath + "receiverTimeout").toMillis
  var receiverBatchSize    : Int    = conf.getInt(confStreamingPath + "receiverBatchSize")

  // Read devices configuration from JSON file
  private[this] lazy val devicesJsonFile                       = conf.getString(confConnPath + "devices")
  private[this] lazy val devicesJson: String                   = File(devicesJsonFile).slurp()
  private[this] lazy val devices    : Array[DeviceCredentials] = parse(devicesJson).extract[Array[DeviceCredentials]]

  def deviceCredentials(id: String): DeviceCredentials = devices.find(x ⇒ x.deviceId == id).get

  if (!Files.exists(Paths.get(devicesJsonFile))) {
    throw new RuntimeException("Devices credentials not found")
  }
}
