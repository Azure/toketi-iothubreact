// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.test.helpers

import java.nio.file.{Files, Paths}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.microsoft.azure.eventhubs.EventHubClient
import com.typesafe.config.{Config, ConfigFactory}

import scala.reflect.io.File

/* Test configuration settings */
private object Configuration {

  private[this] val conf: Config = ConfigFactory.load()

  // Read-only settings
  val iotHubPartitions: Int    = conf.getInt("iothub.partitions")
  val iotHubNamespace : String = conf.getString("iothub.namespace")
  val iotHubName      : String = conf.getString("iothub.name")
  val iotHubKeyName   : String = conf.getString("iothub.keyName")
  val iotHubKey       : String = conf.getString("iothub.key")

  // Tests can override these
  var iotReceiverConsumerGroup: String = EventHubClient.DEFAULT_CONSUMER_GROUP_NAME
  var receiverTimeout         : Long   = conf.getDuration("iothub-stream.receiverTimeout").toMillis
  var receiverBatchSize       : Int    = conf.getInt("iothub-stream.receiverBatchSize")

  // Read devices configuration from JSON file
  private[this] val jsonParser = new ObjectMapper()
  jsonParser.registerModule(DefaultScalaModule)
  private[this] lazy val devicesJsonFile = conf.getString("iothub.devices")
  private[this] lazy val devicesJson     = File(devicesJsonFile).slurp()
  private[this] lazy val devices         = jsonParser.readValue(devicesJson, classOf[Array[DeviceCredentials]])

  def deviceCredentials(id: String): DeviceCredentials = devices.find(x ⇒ x.deviceId == id).get

  if (!Files.exists(Paths.get(devicesJsonFile))) {
    throw new RuntimeException("Devices credentials not found")
  }
}
