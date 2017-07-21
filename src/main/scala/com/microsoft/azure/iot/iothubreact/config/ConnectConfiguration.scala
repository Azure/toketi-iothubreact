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

  // See: "IoT Hub" ⇒ your hub ⇒ Shared access policies ⇒ key name ⇒ Connection string
  val accessConnString: String

  // See: "IoT Hub" ⇒ your hub ⇒ Shared access policies
  val accessPolicy: String

  // See: "IoT Hub" ⇒ your hub ⇒ Shared access policies
  val accessKey: String
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

  // Conn string format: "HostName=.......azure-devices.net;SharedAccessKeyName=......;SharedAccessKey=......"
  lazy val accessConnString = configData.getString(confConnPath + "accessConnString")
  lazy val accessPolicy     = getAccessPolicy(accessConnString)
  lazy val accessKey        = getAccessKey(accessConnString)

  /** Extract namespace from endpoint string
    * The namespace is used when reading commands feedback
    *
    * @param endpoint Endpoint string
    *
    * @return namespace
    */
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
