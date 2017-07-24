// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.checkpointing

import java.util.concurrent.TimeUnit

import com.microsoft.azure.iot.iothubreact.checkpointing.backends.cassandra.lib.Auth
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

/** Checkpointing configuration interface
  */
trait ICPConfiguration {

  /** Namespace where the table with checkpoint data is stored (e.g. Cassandra keyspace)
    */
  val storageNamespace: String

  /** Type of storage, the value is not case sensitive
    */
  val checkpointBackendType: String

  /** How often checkpoint data is written to the storage
    */
  val checkpointFrequency: FiniteDuration

  /** Checkpointing operations timeout
    */
  val checkpointRWTimeout: FiniteDuration

  /** How many messages to replay after a restart, for each IoT hub partition
    */
  val checkpointCountThreshold: Int

  /** Store a position if its value is older than this amount of time, rounded to seconds
    */
  val checkpointTimeThreshold: FiniteDuration

  /** Whether to use the Azure Storage Emulator when using Azure blob backend
    */
  val azureBlobEmulator: Boolean

  /** Azure blob connection string
    */
  val azureBlobConnectionString: String

  /** Azure blob lease duration (between 15s and 60s by Azure docs)
    */
  val azureBlobLeaseDuration: FiniteDuration

  /** Cassandra cluster address
    * TODO: support list
    */
  val cassandraCluster: String

  /** Cassandra replication factor, value required to open a connection
    */
  val cassandraReplicationFactor: Int

  /** Cassandra authentication credentials
    */
  val cassandraAuth: Option[Auth]

  /** CosmosDb SQL endpoint URI
    */
  val cosmosDbSqlUri: String

  /** CosmosDb SQL authentication key
    */
  val cosmosDbSqlkey: String
}

object CPConfiguration {

  def apply(): ICPConfiguration = new CPConfiguration()

  def apply(configData: Config): ICPConfiguration = new CPConfiguration(configData)
}

/** Hold IoT Hub stream checkpointing configuration settings
  */
class CPConfiguration(configData: Config) extends ICPConfiguration {

  // Parameterless ctor
  def this() = this(ConfigFactory.load)

  // TODO: Allow to use multiple configurations, e.g. while processing multiple streams
  //       a client will need a dedicated checkpoint container for each stream

  private[this] val confPath = "iothub-react.checkpointing."

  // Default time between checkpoint writes to the storage
  private[this] val DefaultFrequency = 1 second

  // Minimum time between checkpoints
  private[this] val MinFrequency = 1 second

  // Maximum time between checkpoints
  private[this] val MaxFrequency = 60 seconds

  // Default timeout for checkpoint operations
  private[this] val DefaultStorageRWTimeout = 10 seconds

  // Minimuim timeout for checkpoint operations
  private[this] val MinStorageRWTimeout = 1 seconds

  // Maximum timeout for checkpoint operations
  private[this] val MaxStorageRWTimeout = 60 seconds

  // Default duration of the lock on the checkpoint resources
  private[this] val DefaultLease = 10 seconds

  // Minimuim duration of the lock on the checkpoint resources
  private[this] val MinLease = 15 seconds

  // Maximum duration of the lock on the checkpoint resources
  private[this] val MaxLease = 60 seconds

  // Default time waited before flushing an offset to storage
  private[this] val DefaultTimeThreshold = 5 minutes

  // Minimuim time waited before flushing an offset to storage
  private[this] val MinTimeThreshold = 1 second

  // Maximum time waited before flushing an offset to storage
  private[this] val MaxTimeThreshold = 1 hour

  // Default name of the container used to store checkpoint data
  private[this] lazy val DefaultContainer = checkpointBackendType.toUpperCase match {
    case "CASSANDRA" ⇒ "iothub_react_checkpoints"
    case _           ⇒ "iothub-react-checkpoints"
  }

  // How often checkpoint data is written to the storage
  lazy val checkpointFrequency: FiniteDuration = getDuration(
    confPath + "frequency",
    DefaultFrequency,
    MinFrequency,
    MaxFrequency)

  // How many messages to replay after a restart, for each IoT hub partition
  lazy val checkpointCountThreshold: Int = Math.max(1, configData.getInt(confPath + "countThreshold"))

  // Store a position if its value is older than this amount of time, rounded to seconds
  // Min: 1 second, Max: 1 hour
  lazy val checkpointTimeThreshold: FiniteDuration = getDuration(
    confPath + "timeThreshold",
    DefaultTimeThreshold,
    MinTimeThreshold,
    MaxTimeThreshold)

  // Checkpointing operations timeout
  lazy val checkpointRWTimeout: FiniteDuration = getDuration(
    confPath + "storage.rwTimeout",
    DefaultStorageRWTimeout,
    MinStorageRWTimeout,
    MaxStorageRWTimeout)

  // The backend logic used to write, a.k.a. the storage type
  lazy val checkpointBackendType: String = configData.getString(confPath + "storage.backendType")

  // Data container
  lazy val storageNamespace: String = getStorageContainer

  // Whether to use the Azure Storage Emulator when using Azure blob backend
  lazy val azureBlobEmulator: Boolean = configData.getBoolean(confPath + "storage.azureblob.useEmulator")

  // Azure blob connection string
  lazy val azureBlobConnectionString: String = getAzureBlobConnectionString

  // Azure blob lease duration (between 15s and 60s by Azure docs)
  lazy val azureBlobLeaseDuration: FiniteDuration = getDuration(
    confPath + "storage.azureblob.lease",
    15 seconds,
    15 seconds,
    60 seconds)

  // Cassandra cluster address
  lazy val cassandraCluster          : String       = configData.getString(confPath + "storage.cassandra.cluster")
  lazy val cassandraReplicationFactor: Int          = configData.getInt(confPath + "storage.cassandra.replicationFactor")
  lazy val cassandraAuth             : Option[Auth] =
    (for {
      u <- Try(configData.getString(confPath + "storage.cassandra.username"))
      p <- Try(configData.getString(confPath + "storage.cassandra.password"))
    } yield {
      Auth(u, p)
    }).toOption match {
      case Some(x) if !x.username.isEmpty ⇒ Some(x)
      case _                              ⇒ None
    }

  /** CosmosDb SQL endpoint URI
    */
  lazy val cosmosDbSqlUri: String = extractCosmosDbSqlUri(configData.getString(confPath + "storage.cosmosdbsql.connString"))

  /** CosmosDb SQL authentication key
    */
  lazy val cosmosDbSqlkey: String = extractCosmosDbSqlKey(configData.getString(confPath + "storage.cosmosdbsql.connString"))

  /** Load Azure blob connection string, taking care of the Azure storage emulator case
    *
    * @return Connection string
    */
  private[this] def getAzureBlobConnectionString: String = {
    if (configData.getBoolean(confPath + "storage.azureblob.useEmulator"))
      ""
    else {
      val protocol = configData.getString(confPath + "storage.azureblob.protocol")
      val account = configData.getString(confPath + "storage.azureblob.account")
      val key = configData.getString(confPath + "storage.azureblob.key")
      s"DefaultEndpointsProtocol=${protocol};AccountName=${account};AccountKey=${key}";
    }
  }

  /** Get the name of the table/container/path etc where data is stored
    *
    * @return Table/Container/Path name
    */
  private[this] def getStorageContainer: String = {
    val container = configData.getString(confPath + "storage.namespace")
    if (container != "")
      container
    else
      DefaultContainer
  }

  /** Get the duration of the Azure blob leases
    *
    * @return Lease duration in seconds
    */
  private[this] def getDuration(
      path: String,
      default: FiniteDuration,
      min: FiniteDuration,
      max: FiniteDuration): FiniteDuration = {

    val value = configData.getDuration(path, TimeUnit.MILLISECONDS)
    if (value >= min.toMillis && value <= max.toMillis)
      FiniteDuration(value, TimeUnit.MILLISECONDS)
    else
      default
  }

  private[this] def extractCosmosDbSqlUri(text: String): String = {
    """.*AccountEndpoint=(.*);.*""".r
      .findFirstMatchIn(text)
      .map(_ group 1)
      .getOrElse("https://ENDPOINT-NOT-FOUND.documents.azure.com:443/")
  }

  private[this] def extractCosmosDbSqlKey(text: String): String = {
    """.*AccountKey=(.*);""".r
      .findFirstMatchIn(text)
      .map(_ group 1)
      .getOrElse("")
  }
}
