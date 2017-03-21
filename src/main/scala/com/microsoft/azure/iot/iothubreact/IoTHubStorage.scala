// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

import com.microsoft.azure.eventhubs.EventHubClient
import com.microsoft.azure.servicebus.ConnectionStringBuilder

private case class IoTHubStorage(config: IConfiguration) extends Logger {

  // TODO: Manage transient errors e.g. timeouts
  // EventHubClient.createFromConnectionString(connString)
  //   .get(config.receiverTimeout, TimeUnit.MILLISECONDS)
  def createClient(): EventHubClient = {
    log.info(s"Creating EventHub client to ${config.iotHubName}")
    EventHubClient.createFromConnectionStringSync(buildConnString())
  }

  private[this] def buildConnString() =
    new ConnectionStringBuilder(
      config.iotHubNamespace,
      config.iotHubName,
      config.accessPolicy,
      config.accessKey).toString
}
