// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.config

import java.util.concurrent.TimeUnit

import com.microsoft.azure.eventhubs.EventHubClient
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps

trait IStreamConfiguration {

  // Hub receiver group name. See: "IoT Hub" >> your hub > "Messaging" >> Consumer groups
  // @see https://azure.microsoft.com/en-us/documentation/articles/event-hubs-overview
  val receiverConsumerGroup: String

  // Hub message receiver timeout.
  val receiverTimeout: FiniteDuration

  // How many messages to retrieve on each pull, max is 999.
  val receiverBatchSize: Int

  // Whether to retrieve information about the partitions while streming events from IoT Hub.
  val retrieveRuntimeMetric: Boolean
}

object StreamConfiguration {

  def apply(): IStreamConfiguration = new StreamConfiguration()

  def apply(configData: Config): IStreamConfiguration = new StreamConfiguration(configData)
}

/** Hold IoT Hub React streaming settings
  */
class StreamConfiguration(configData: Config) extends IStreamConfiguration {

  // Parameterless ctor
  def this() = this(ConfigFactory.load)

  private[this] val confStreamingPath = "iothub-react.streaming."

  // Default IoThub client timeout
  private[this] val DefaultReceiverTimeout = 3 seconds

  // Maximum size supported by the client
  private[this] val MaxBatchSize = 999

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
}
