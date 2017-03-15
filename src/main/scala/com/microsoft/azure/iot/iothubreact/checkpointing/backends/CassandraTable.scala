// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.checkpointing.backends

import com.microsoft.azure.iot.iothubreact.Logger
import com.microsoft.azure.iot.iothubreact.checkpointing.ICPConfiguration
import com.microsoft.azure.iot.iothubreact.checkpointing.backends.cassandra.lib.Connection
import com.microsoft.azure.iot.iothubreact.checkpointing.backends.cassandra.{CheckpointRecord, CheckpointsTableSchema}
import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHubPartition
import org.json4s.JsonAST

/** Storage logic to write checkpoints to a Cassandra table
  */
private[iothubreact] class CassandraTable(implicit val config: ICPConfiguration) extends CheckpointBackend with Logger {

  val schema     = new CheckpointsTableSchema(checkpointNamespace, "checkpoints")
  val connection = Connection(config.cassandraCluster, config.cassandraReplicationFactor, config.cassandraAuth, schema)
  val table      = connection.getTable[CheckpointRecord]()

  connection.createKeyspaceIfNotExists()
  connection.createTableIfNotExists()

  /** Read the offset of the last record processed for the given partition
    *
    * @param partition IoT hub partition number
    *
    * @return Offset of the last record (already) processed
    */
  override def readOffset(partition: Int): String = {
    val result: JsonAST.JObject = table.select(s"partition = ${partition}")

    if (result.values("partition").asInstanceOf[BigInt] < 0) {
      IoTHubPartition.OffsetCheckpointNotFound
    } else {
      result.values("offset").asInstanceOf[String]
    }
  }

  /** Store the offset for the given IoT hub partition
    *
    * @param partition IoT hub partition number
    * @param offset    IoT hub partition offset
    */
  override def writeOffset(partition: Int, offset: String): Unit = {
    table.updateRow(CheckpointRecord(partition, offset))
  }
}
