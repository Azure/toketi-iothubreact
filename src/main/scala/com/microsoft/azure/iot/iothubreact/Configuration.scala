// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

import java.util.concurrent.TimeUnit

import com.microsoft.azure.eventhubs.EventHubClient
import com.microsoft.azure.iot.iothubreact.checkpointing.{CPConfiguration, ICPConfiguration}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.language.postfixOps

trait IConfiguration {
  // Hub name. See: Endpoints ⇒ Messaging ⇒ Events ⇒ "Event Hub-compatible name"
  val iotHubName: String

  // Hub namespace, extracted from the endpoint. See: Endpoints ⇒ Messaging ⇒ Events ⇒ "Event Hub-compatible endpoint"
  val iotHubNamespace: String

  // Hub storage partitions number. See: Endpoints ⇒ Messaging ⇒ Events ⇒ Partitions
  val iotHubPartitions: Int

  // Hub access policy name. See: "IoT Hub" ⇒ your hub ⇒ "Shared access policies"
  val accessPolicy: String

  // Hub access policy key. see: Shared access policies ⇒ key name ⇒ Primary key (or secondary)
  val accessKey: String

  // Hostname used to send messages. See: Shared access policies ⇒ key name ⇒ Connection string ⇒ "HostName"
  val accessHostname: String

  // Hub receiver group name. See: "IoT Hub" >> your hub > "Messaging" >> Consumer groups
  // @see https://azure.microsoft.com/en-us/documentation/articles/event-hubs-overview
  val receiverConsumerGroup: String

  // Hub message receiver timeout.
  val receiverTimeout: FiniteDuration

  // How many messages to retrieve on each pull, max is 999.
  val receiverBatchSize: Int

  // Whether to retrieve information about the partitions while streming events from IoT Hub.
  val retrieveRuntimeMetric: Boolean

  // Return the checkpointing configuration
  val cpConfig: ICPConfiguration
}

/** Hold IoT Hub configuration settings
  *
  * @see https://github.com/typesafehub/config for information about the configuration file formats
  */
private[iothubreact] case class Configuration(configData: Config = ConfigFactory.load) extends IConfiguration {

  // Configuration paths.
  private[this] val confConnPath      = "iothub-react.connection."
  private[this] val confStreamingPath = "iothub-react.streaming."

  // Maximum size supported by the client
  private[this] val MaxBatchSize = 999

  // Default IoThub client timeout
  private[this] val DefaultReceiverTimeout = 3 seconds

  lazy val iotHubName      : String = configData.getString(confConnPath + "hubName")
  lazy val iotHubNamespace : String = getNamespaceFromEndpoint(configData.getString(confConnPath + "hubEndpoint"))
  lazy val iotHubPartitions: Int    = configData.getInt(confConnPath + "hubPartitions")
  lazy val accessPolicy    : String = configData.getString(confConnPath + "accessPolicy")
  lazy val accessKey       : String = configData.getString(confConnPath + "accessKey")
  lazy val accessHostname  : String = configData.getString(confConnPath + "accessHostName")

  lazy val receiverConsumerGroup = configData.getString(confStreamingPath + "consumerGroup") match {
    case x if (x.toUpperCase.matches("DEFAULT|$DEFAULT")) ⇒ EventHubClient.DEFAULT_CONSUMER_GROUP_NAME
    case y                                                ⇒ y
  }

  lazy val receiverTimeout = configData.getDuration(confStreamingPath + "receiverTimeout").toMillis match {
    case x if x > 0 ⇒ FiniteDuration(x, TimeUnit.MILLISECONDS)
    case _          ⇒ DefaultReceiverTimeout
  }

  lazy val receiverBatchSize = configData.getInt(confStreamingPath + "receiverBatchSize") match {
    case x if (x > 0 && x <= MaxBatchSize) ⇒ x
    case _                                 ⇒ MaxBatchSize
  }

  lazy val retrieveRuntimeMetric = configData.getBoolean(confStreamingPath + "retrieveRuntimeMetric")

  lazy val cpConfig: ICPConfiguration = new CPConfiguration(configData)

  // Load data from application.conf and reference.conf
  //private[this] lazy val conf: Config = ConfigFactory.load()

  /** Extract namespace from endpoint string
    *
    * @param endpoint Endpoint string
    *
    * @return namespace
    */
  private[this] def getNamespaceFromEndpoint(endpoint: String): String = {
    endpoint.replaceFirst(".*://", "").replaceFirst("\\..*", "")
  }
}
