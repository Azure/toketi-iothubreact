// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.checkpointing.backends

import com.microsoft.azure.documentdb._
import com.microsoft.azure.iot.iothubreact.Logger
import com.microsoft.azure.iot.iothubreact.checkpointing.ICPConfiguration
import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHubPartition

/**
  * Storage logic to write checkpoints to CosmosDb SQL
  */
private[iothubreact] class CosmosDbSql(cpconfig: ICPConfiguration) extends CheckpointBackend with Logger {

  log.debug("New instance of CosmosDbSql")

  // https://docs.microsoft.com/en-us/azure/cosmos-db/consistency-levels
  val connection = new DocumentClient(
    cpconfig.cosmosDbSqlUri,
    cpconfig.cosmosDbSqlkey,
    ConnectionPolicy.GetDefault(),
    ConsistencyLevel.Eventual)

  val dbName   = cpconfig.storageNamespace
  val collName = cpconfig.storageNamespace
  this.createDatabaseIfNotExists(dbName)
  this.createCollectionIfNotExists(collName)

  /** Read the offset of the last record processed for the given partition
    *
    * @param partition IoT hub partition number
    *
    * @return Offset of the last record (already) processed
    */
  override def readOffset(partition: Int): String = {
    try {
      val docId = getId(partition)
      this.connection.readDocument(s"/dbs/$dbName/colls/$collName/docs/$docId", null)
        .getResource().getString("offset")
    }
    catch {
      case e: DocumentClientException if e.getStatusCode == 404 ⇒
        IoTHubPartition.OffsetCheckpointNotFound

      case e: Exception ⇒
        log.error(e, e.getMessage)
        throw e
    }
  }

  /** Store the offset for the given IoT hub partition
    *
    * @param partition IoT hub partition number
    * @param offset    IoT hub partition offset
    */
  override def writeOffset(partition: Int, offset: String): Unit = {
    var doc = new Document()
    doc.setId(getId(partition))
    doc.set("partition", partition)
    doc.set("offset", offset)

    try
      this.connection.upsertDocument(s"/dbs/$dbName/colls/$collName", doc, null, true)
    catch {
      case e: DocumentClientException if e.getStatusCode == 404 ⇒
        // Recreate DB and Collection in case they were deleted
        this.createDatabaseIfNotExists(dbName)
        this.createCollectionIfNotExists(collName)

      case e: Exception ⇒
        log.error(e, e.getMessage)
        throw e
    }
  }

  private[this] def getId(partition: Int) = "partition" + partition

  private[this] def createDatabaseIfNotExists(name: String): Unit = {

    val databaseLink = String.format("/dbs/%s", name)

    try
      this.connection.readDatabase(databaseLink, null)
    catch {
      case e: DocumentClientException if e.getStatusCode == 404 ⇒
        import com.microsoft.azure.documentdb.Database
        val database = new Database
        database.setId(name)
        this.connection.createDatabase(database, null)

      case e: Exception ⇒
        log.error(e, e.getMessage)
        throw e
    }
  }

  private[this] def createCollectionIfNotExists(name: String): Unit = {

    val databaseLink = String.format("/dbs/%s", name)
    val collectionLink = String.format("/dbs/%s/colls/%s", name, name)

    try
      this.connection.readCollection(collectionLink, null)
    catch {
      case e: DocumentClientException if e.getStatusCode == 404 ⇒
        import com.microsoft.azure.documentdb.DocumentCollection
        val collectionInfo = new DocumentCollection
        collectionInfo.setId(name)
        val requestOptions = new RequestOptions()
        requestOptions.setOfferThroughput(400)
        this.connection.createCollection(databaseLink, collectionInfo, requestOptions)

      case e: Exception ⇒
        log.error(e, e.getMessage)
        throw e
    }
  }
}
