// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.config

import com.typesafe.config.{Config, ConfigFactory}

trait IConnectConfiguration {

  // Hub namespace, extracted from the endpoint. See: Endpoints ⇒ Messaging ⇒ Events ⇒ "Event Hub-compatible endpoint"
  val iotHubNamespace: String

  // Hub name. See: Endpoints ⇒ Messaging ⇒ Events ⇒ "Event Hub-compatible name"
  val iotHubName: String

  // Hub storage partitions number. See: Endpoints ⇒ Messaging ⇒ Events ⇒ Partitions
  val iotHubPartitions: Int

  // Hub access policy name. See: "IoT Hub" ⇒ your hub ⇒ "Shared access policies"
  val accessPolicy: String

  // Hub access policy key. see: Shared access policies ⇒ key name ⇒ Primary key (or secondary)
  val accessKey: String

  // Hostname used to send messages. See: Shared access policies ⇒ key name ⇒ Connection string ⇒ "HostName"
  val accessHostname: String
}

object ConnectConfiguration {

  def apply(): IConnectConfiguration = new ConnectConfiguration()

  def apply(configData: Config): IConnectConfiguration = new ConnectConfiguration(configData)
}

/** Hold settings to connect to IoT Hub
  */
class ConnectConfiguration(configData: Config) extends IConnectConfiguration {

  // Parameterless ctor
  def this() = this(ConfigFactory.load)

  private[this] val confConnPath = "iothub-react.connection."

  lazy val iotHubNamespace  = getNamespaceFromEndpoint(configData.getString(confConnPath + "hubEndpoint"))
  lazy val iotHubName       = configData.getString(confConnPath + "hubName")
  lazy val iotHubPartitions = configData.getInt(confConnPath + "hubPartitions")
  lazy val accessPolicy     = configData.getString(confConnPath + "accessPolicy")
  lazy val accessKey        = configData.getString(confConnPath + "accessKey")
  lazy val accessHostname   = configData.getString(confConnPath + "accessHostName")

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
