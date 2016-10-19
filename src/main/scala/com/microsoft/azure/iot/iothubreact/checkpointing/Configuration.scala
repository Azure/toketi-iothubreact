// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.checkpointing

import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.language.postfixOps

/** Hold IoT Hub stream checkpointing configuration settings
  *
  * @todo Allow to use multiple configurations, for instance while processing multiple
  *       streams a client will need a dedicated checkpoint container for each stream
  */
private[iothubreact] object Configuration {

  private[this] val conf: Config = ConfigFactory.load()

  // Default time between checkpoint writes to the storage
  private[this] val DefaultFrequency = 1 second

  // Minimum time between checkpoints
  private[this] val MinFrequency = 10 milli

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

  // Default name of the container used to store checkpoint data
  private[this] val DefaultContainer = "iothub-react-checkpoints"

  // Whether checkpointing is enabled or not
  lazy val isEnabled: Boolean = conf.getBoolean("iothub-checkpointing.enabled")

  // How often checkpoint data is written to the storage
  lazy val checkpointingFrequency: FiniteDuration = getDuration(
    "iothub-checkpointing.frequency",
    DefaultFrequency,
    MinFrequency,
    MaxFrequency)

  // Checkpointing operations timeout
  lazy val checkpointsRWTimeout: FiniteDuration = getDuration(
    "iothub-checkpointing.storage.rwTimeout",
    DefaultStorageRWTimeout,
    MinStorageRWTimeout,
    MaxStorageRWTimeout)

  // The backend logic used to write, a.k.a. the storage type
  lazy val checkpointingClass: String = conf.getString("iothub-checkpointing.storage.class")

  // Whether to use the Azure Storage Emulator when using Azure blob backend
  lazy val azureBlobEmulator: Boolean = conf.getBoolean("iothub-checkpointing.storage.azureblob.useEmulator")

  // Azure blob connection string
  lazy val azureBlobConnectionString: String = getAzureBlobConnectionString

  // Azure blob container
  lazy val azureBlobContainer: String = getAzureBlobContainer

  // Azure blob lease duration (15s and 60s by Azure docs)
  lazy val azureBlobLeaseDuration: FiniteDuration = getDuration(
    "iothub-checkpointing.storage.azureblob.lease",
    15 seconds,
    15 seconds,
    60 seconds)

  /** Load Azure blob connection string, taking care of the Azure storage emulator case
    *
    * @return Connection string
    */
  private[this] def getAzureBlobConnectionString: String = {
    if (conf.getBoolean("iothub-checkpointing.storage.azureblob.useEmulator"))
      ""
    else {
      val protocol = conf.getString("iothub-checkpointing.storage.azureblob.protocol")
      val account = conf.getString("iothub-checkpointing.storage.azureblob.account")
      val key = conf.getString("iothub-checkpointing.storage.azureblob.key")
      s"DefaultEndpointsProtocol=${protocol};AccountName=${account};AccountKey=${key}";
    }
  }

  /** Get the name of the Azure blob container
    *
    * @return Container name
    */
  private[this] def getAzureBlobContainer: String = {
    val container = conf.getString("iothub-checkpointing.storage.azureblob.container")
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
