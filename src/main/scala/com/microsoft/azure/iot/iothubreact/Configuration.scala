// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

import java.util.concurrent.TimeUnit

import com.microsoft.azure.eventhubs.EventHubClient
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.language.postfixOps

/** Hold IoT Hub configuration settings
  *
  * @see https://github.com/typesafehub/config for information about the
  *      configuration file formats
  * @todo dependency injection
  */
private[iothubreact] object Configuration {

  // Maximum size supported by the client
  private[this] val MaxBatchSize = 999

  // Default IoThub client timeout
  private[this] val DefaultReceiverTimeout = 3 seconds

  private[this] val conf: Config = ConfigFactory.load()

  // IoT hub storage details
  val iotHubPartitions: Int    = conf.getInt("iothub.partitions")
  val iotHubNamespace : String = conf.getString("iothub.namespace")
  val iotHubName      : String = conf.getString("iothub.name")
  val iotHubKeyName   : String = conf.getString("iothub.keyName")
  val iotHubKey       : String = conf.getString("iothub.key")

  // Consumer group used to retrieve messages
  // @see https://azure.microsoft.com/en-us/documentation/articles/event-hubs-overview
  private[this] val tmpCG = conf.getString("iothub.consumerGroup")
  val receiverConsumerGroup: String =
    tmpCG match {
      case "$Default" ⇒ EventHubClient.DEFAULT_CONSUMER_GROUP_NAME
      case "Default"  ⇒ EventHubClient.DEFAULT_CONSUMER_GROUP_NAME
      case "default"  ⇒ EventHubClient.DEFAULT_CONSUMER_GROUP_NAME
      case "DEFAULT"  ⇒ EventHubClient.DEFAULT_CONSUMER_GROUP_NAME
      case _          ⇒ tmpCG
    }

  // Message retrieval timeout in milliseconds
  private[this] val tmpRTO = conf.getDuration("iothub-stream.receiverTimeout").toMillis
  val receiverTimeout: FiniteDuration =
    if (tmpRTO > 0)
      FiniteDuration(tmpRTO, TimeUnit.MILLISECONDS)
    else
      DefaultReceiverTimeout

  // How many messages to retrieve on each call to the storage
  private[this] val tmpRBS = conf.getInt("iothub-stream.receiverBatchSize")
  val receiverBatchSize: Int =
    if (tmpRBS > 0 && tmpRBS <= MaxBatchSize)
      tmpRBS
    else
      MaxBatchSize
}
