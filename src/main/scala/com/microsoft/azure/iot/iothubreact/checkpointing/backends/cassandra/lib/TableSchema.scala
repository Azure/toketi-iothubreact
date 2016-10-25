// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.checkpointing.backends.cassandra.lib

/** Interface to be implemented to define the schema of the table
  */
private[iothubreact] trait TableSchema {
  protected[this] val keyspace: String
  protected[this] val name    : String
  val columns: Seq[Column]

  // Hide the original values and expose the filtered ones
  // https://docs.datastax.com/en/cql/3.3/cql/cql_reference/ref-lexical-valid-chars.html
  lazy val keyspaceCQL = this.keyspace.replaceAll("[^A-Za-z0-9_]", "_")
  lazy val nameCQL     = this.name.replaceAll("[^A-Za-z0-9_]", "_")
}
