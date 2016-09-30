// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

import com.microsoft.azure.eventhubs.EventHubClient
import com.typesafe.config.{Config, ConfigFactory}

/** Hold IoT Hub configuration settings
  *
  * @see https://github.com/typesafehub/config for information about the
  *      configuration file formats
  * @todo dependency injection
  */
private object Configuration {

  // Maximum size supported by the client
  private[this] val MaxBatchSize = 999

  // Maximum size supported by the client
  private[this] val DefaultReceiverTimeout = 3000

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
  private[this] val tmpRTO = conf.getDuration("iothub.receiverTimeout").toMillis
  val receiverTimeout: Long =
    if (tmpRTO > 0)
      tmpRTO
    else
      DefaultReceiverTimeout

  // How many messages to retrieve on each call to the storage
  private[this] val tmpRBS = conf.getInt("iothub.receiverBatchSize")
  val receiverBatchSize: Int =
    if (tmpRBS > 0 && tmpRBS <= MaxBatchSize)
      tmpRBS
    else
      MaxBatchSize
}
