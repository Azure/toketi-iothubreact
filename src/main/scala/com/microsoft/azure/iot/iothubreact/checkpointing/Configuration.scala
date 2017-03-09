// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.checkpointing

import java.util.concurrent.TimeUnit

import com.microsoft.azure.iot.iothubreact.checkpointing.backends.AzureBlob
import com.microsoft.azure.iot.iothubreact.checkpointing.backends.cassandra.lib.Auth
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

/** Hold IoT Hub stream checkpointing configuration settings
  */
final class Configuration(loader: Config = ConfigFactory.load) {

  // TODO: Allow to use multiple configurations, e.g. while processing multiple streams
  //       a client will need a dedicated checkpoint container for each stream

  private[this] val confPath = "iothub-react.checkpointing."

  private[this] val conf: Config = loader

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

  // Whether checkpointing is enabled or not
  lazy val isEnabled: Boolean = conf.getBoolean(confPath + "enabled")

  // How often checkpoint data is written to the storage
  lazy val checkpointFrequency: FiniteDuration = getDuration(
    confPath + "frequency",
    DefaultFrequency,
    MinFrequency,
    MaxFrequency)

  // How many messages to replay after a restart, for each IoT hub partition
  lazy val checkpointCountThreshold = Math.max(1, conf.getInt(confPath + "countThreshold"))

  // Store a position if its value is older than this amount of time, rounded to seconds
  // Min: 1 second, Max: 1 hour
  lazy val checkpointTimeThreshold = getDuration(
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
  lazy val checkpointBackendType: String = conf.getString(confPath + "storage.backendType")

  // Data container
  lazy val storageNamespace: String = getStorageContainer

  // Whether to use the Azure Storage Emulator when using Azure blob backend
  lazy val azureBlobEmulator: Boolean = conf.getBoolean(confPath + "storage.azureblob.useEmulator")

  // Azure blob connection string
  lazy val azureBlobConnectionString: String = getAzureBlobConnectionString

  // Azure blob lease duration (15s and 60s by Azure docs)
  lazy val azureBlobLeaseDuration: FiniteDuration = getDuration(
    confPath + "storage.azureblob.lease",
    15 seconds,
    15 seconds,
    60 seconds)

  // Cassandra cluster address
  lazy val cassandraCluster          : String = conf.getString(confPath + "storage.cassandra.cluster")
  lazy val cassandraReplicationFactor: Int    = conf.getInt(confPath + "storage.cassandra.replicationFactor")
  lazy val cassandraAuth: Option[Auth] = (for {
    u <- Try(conf.getString(confPath + "storage.cassandra.username"))
    p <- Try(conf.getString(confPath + "storage.cassandra.password"))
  } yield {
    Auth(u, p)
  }).toOption

  /** Load Azure blob connection string, taking care of the Azure storage emulator case
    *
    * @return Connection string
    */
  private[this] def getAzureBlobConnectionString: String = {
    if (conf.getBoolean(confPath + "storage.azureblob.useEmulator"))
      ""
    else {
      val protocol = conf.getString(confPath + "storage.azureblob.protocol")
      val account = conf.getString(confPath + "storage.azureblob.account")
      val key = conf.getString(confPath + "storage.azureblob.key")
      s"DefaultEndpointsProtocol=${protocol};AccountName=${account};AccountKey=${key}";
    }
  }

  /** Get the name of the table/container/path etc where data is stored
    *
    * @return Table/Container/Path name
    */
  private[this] def getStorageContainer: String = {
    val container = conf.getString(confPath + "storage.namespace")
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

    val value = conf.getDuration(path, TimeUnit.MILLISECONDS)
    if (value >= min.toMillis && value <= max.toMillis)
      FiniteDuration(value, TimeUnit.MILLISECONDS)
    else
      default
  }
}
