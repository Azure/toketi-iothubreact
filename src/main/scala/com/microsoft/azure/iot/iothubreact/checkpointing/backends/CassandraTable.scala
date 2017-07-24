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
private[iothubreact] class CassandraTable(cpconfig: ICPConfiguration) extends CheckpointBackend with Logger {

  log.debug("New instance of CassandraTable")

  val schema     = new CheckpointsTableSchema(checkpointNamespace(cpconfig), "checkpoints")
  val connection = Connection(cpconfig.cassandraCluster, cpconfig.cassandraReplicationFactor, cpconfig.cassandraAuth, schema)
  val table      = connection.getTable[CheckpointRecord]()

  // Note: if these were to run concurrently, with multiple attempts to create keyspace+table,
  // Cassandra doesn't seem to respond well to that and crashes due to contentions
  connection.createKeyspaceIfNotExists()
  connection.createTableIfNotExists()

  /** Read the offset of the last record processed for the given partition
    *
    * @param partition IoT hub partition number
    *
    * @return Offset of the last record (already) processed
    */
  override def readOffset(endpoint: String, partition: Int): String = {
    val result: JsonAST.JObject = table.select(s"endpoint = '${endpoint}' and partition = ${partition}")

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
  override def writeOffset(endpoint: String, partition: Int, offset: String): Unit = {
    table.updateRow(CheckpointRecord(endpoint, partition, offset))
  }
}
