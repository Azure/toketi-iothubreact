// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.checkpointing.backends

import java.io.IOException
import java.net.URISyntaxException
import java.util.UUID

import com.microsoft.azure.iot.iothubreact.checkpointing.ICPConfiguration
import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHubPartition
import com.microsoft.azure.iot.iothubreact.{Logger, Retry}
import com.microsoft.azure.storage.blob.CloudBlockBlob
import com.microsoft.azure.storage.{AccessCondition, CloudStorageAccount, OperationContext, StorageException}

import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}

/** Storage logic to write checkpoints to Azure blobs
  */
private[iothubreact] class AzureBlob(implicit val config: ICPConfiguration) extends CheckpointBackend with Logger {

  // Set the account to point either to Azure or the emulator
  val account: CloudStorageAccount = if (config.azureBlobEmulator)
                                       CloudStorageAccount.getDevelopmentStorageAccount()
                                     else
                                       CloudStorageAccount.parse(config.azureBlobConnectionString)

  val client = account.createCloudBlobClient()

  // Set the container, ensure it's ready
  val container = client.getContainerReference(checkpointNamespace)
  try {
    Retry(2, 5 seconds) {
      container.createIfNotExists()
    }
  } catch {
    case e: StorageException ⇒ {
      log.error(e, s"Err: ${e.getMessage}; Code: ${e.getErrorCode}; Status: ${e.getHttpStatusCode}")
      throw e
    }

    case e: IOException ⇒ {
      log.error(e, e.getMessage)
      throw e
    }

    case e: Exception ⇒ {
      log.error(e, e.getMessage)
      throw e
    }
  }

  /** Read the offset of the last record processed for the given partition
    *
    * @param partition Partition number
    *
    * @return Offset of the last record (already) processed
    */
  override def readOffset(partition: Int): String = {
    val file = getBlockBlobReference(partition)
    try {
      file.downloadText()
    } catch {
      case e: StorageException ⇒ {
        if (e.getErrorCode == "BlobNotFound") {
          IoTHubPartition.OffsetCheckpointNotFound
        } else {
          log.error(e, s"Err: ${e.getMessage}; Code: ${e.getErrorCode}; Status: ${e.getHttpStatusCode}")
          throw e
        }
      }

      case e: IOException ⇒ {
        log.error(e, e.getMessage)
        throw e
      }

      case e: Exception ⇒ {
        log.error(e, e.getMessage)
        throw e
      }
    }
  }

  /** Store the offset for the given IoT hub partition
    *
    * @param partition IoT hub partition number
    * @param offset    IoT hub partition offset
    */
  override def writeOffset(partition: Int, offset: String): Unit = {
    val file = getBlockBlobReference(partition)
    val leaseId = acquireLease(file)
    writeAndRelease(file, leaseId, offset)
  }

  private[this] def getBlockBlobReference(partition: Int): CloudBlockBlob = {
    try {
      Retry(2, 2 seconds) {
        container.getBlockBlobReference(filename(partition))
      }
    } catch {

      case e: StorageException ⇒ {
        log.error(e, e.getMessage)
        throw e
      }

      case e: URISyntaxException ⇒ {
        log.error(e, e.getMessage)
        throw e
      }

      case e: Exception ⇒ {
        log.error(e, e.getMessage)
        throw e
      }
    }
  }

  private[this] def acquireLease(file: CloudBlockBlob): String = {
    // Note: the lease ID must be a Guid otherwise the service returs 400
    var leaseId = UUID.randomUUID().toString
    try {
      file.acquireLease(config.azureBlobLeaseDuration.toSeconds.toInt, leaseId)
    } catch {

      case e: StorageException ⇒ {
        if (e.getErrorCode == "BlobNotFound") {
          leaseId = ""
        } else {
          log.error(e, s"Err: ${e.getMessage}; Code: ${e.getErrorCode}; Status: ${e.getHttpStatusCode}")
          throw e
        }
      }

      case e: Exception ⇒ {
        log.error(e, e.getMessage)
        throw e
      }
    }

    leaseId
  }

  private[this] def writeAndRelease(file: CloudBlockBlob, leaseId: String, content: String): Unit = {

    // The access condition depends on the file existing
    val accessCondition = if (leaseId == "")
                            AccessCondition.generateEmptyCondition()
                          else
                            AccessCondition.generateLeaseCondition(leaseId)

    try {
      file.uploadText(content, "UTF-8", accessCondition, null, new OperationContext)

      // If this is a new file, there is no lease to release
      if (leaseId != "") file.releaseLease(accessCondition)
    } catch {

      case e: StorageException ⇒ {
        log.error(e, e.getMessage)
        throw e
      }

      case e: IOException ⇒ {
        log.error(e, e.getMessage)
        throw e
      }

      case e: Exception ⇒ {
        log.error(e, e.getMessage)
        throw e
      }
    }
  }

  private[this] def filename(partition: Int): String = "partition-" + partition
}
