// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.checkpointing.backends.cassandra

import com.microsoft.azure.iot.iothubreact.checkpointing.backends.cassandra.lib._

/** Schema of the table containing the checkpoints
  */
private[iothubreact] class CheckpointsTableSchema(keySpace: String, tableName: String) extends TableSchema {

  // Container name
  override val keyspace: String = keySpace

  // Table name
  override val name: String = tableName

  // Columns
  override val columns: Seq[Column] = Seq(
    Column("endpoint", ColumnType.String, true),
    Column("partition", ColumnType.Int, true),
    Column("offset", ColumnType.String),
    Column("lastUpdate", ColumnType.Timestamp)
  )
}
