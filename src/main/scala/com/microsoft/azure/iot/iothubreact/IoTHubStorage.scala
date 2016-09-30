// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

import com.microsoft.azure.eventhubs.EventHubClient
import com.microsoft.azure.servicebus.ConnectionStringBuilder

private object IoTHubStorage {

  private[this] val connString = new ConnectionStringBuilder(
    Configuration.iotHubNamespace,
    Configuration.iotHubName,
    Configuration.iotHubKeyName,
    Configuration.iotHubKey).toString

  // @todo Manage transient errors e.g. timeouts
  // EventHubClient.createFromConnectionString(connString)
  //   .get(Configuration.receiverTimeout, TimeUnit.MILLISECONDS)
  def createClient(): EventHubClient = EventHubClient.createFromConnectionStringSync(connString)
}
