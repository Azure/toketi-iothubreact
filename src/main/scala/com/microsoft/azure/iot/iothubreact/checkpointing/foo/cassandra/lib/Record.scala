// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.checkpointing.backends.cassandra.lib

/** Trait to be implemented by records to be stored into Cassandra
  */
private[iothubreact] trait ToCassandra {
  def toJsonValues: String
}
