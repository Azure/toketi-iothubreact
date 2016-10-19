// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.checkpointing.Backends

import java.io.IOException
import java.net.URISyntaxException
import java.util.UUID

import com.microsoft.azure.iot.iothubreact.Logger
import com.microsoft.azure.iot.iothubreact.checkpointing.Configuration
import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHub
import com.microsoft.azure.storage.blob.CloudBlockBlob
import com.microsoft.azure.storage.{AccessCondition, CloudStorageAccount, OperationContext, StorageException}

private[iothubreact] class AzureBlob extends CheckpointBackend with Logger {

  val account: CloudStorageAccount = if (Configuration.azureBlobEmulator)
    CloudStorageAccount.getDevelopmentStorageAccount()
  else
    CloudStorageAccount.parse(Configuration.azureBlobConnectionString)

  val client    = account.createCloudBlobClient()
  val container = client.getContainerReference(checkpointNamespace)
  try {
    container.createIfNotExists()
  } catch {
    case e: StorageException ⇒ {
      log.error(e, s"Err: ${e.getMessage}; Code: ${e.getErrorCode}; Status: ${e.getHttpStatusCode}")
      throw e
    }
    case e: IOException      ⇒ {
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
          "" //IoTHub.OffsetNotFound
        } else {
          log.error(e, s"Err: ${e.getMessage}; Code: ${e.getErrorCode}; Status: ${e.getHttpStatusCode}")
          throw e
        }
      }
      case e: IOException      ⇒ {
        log.error(e, e.getMessage)
        throw e
      }
      case e: Exception        ⇒ {
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
      container.getBlockBlobReference(filename(partition))
    } catch {
      // @todo manage exceptions
      case e: StorageException   ⇒ {
        println(e)
        throw e
      }
      case e: URISyntaxException ⇒ {
        println(e)
        throw e
      }
      case e: Exception          ⇒ {
        println(e)
        throw e
      }
    }
  }

  private[this] def acquireLease(file: CloudBlockBlob): String = {
    var leaseId = UUID.randomUUID().toString
    try {
      file.acquireLease(Configuration.azureBlobLeaseDuration.toSeconds.toInt, leaseId)
    } catch {
      case e: StorageException ⇒ {
        if (e.getErrorCode == "BlobNotFound") {
          leaseId = ""
        } else {
          log.error(e, s"Err: ${e.getMessage}; Code: ${e.getErrorCode}; Status: ${e.getHttpStatusCode}")
          throw e
        }
      }
      case e: Exception        ⇒ {
        log.error(e, e.getMessage)
        throw e
      }
    }

    leaseId
  }

  private[this] def writeAndRelease(file: CloudBlockBlob, leaseId: String, content: String): Unit = {
    val accessCondition = if (leaseId == "")
      AccessCondition.generateEmptyCondition()
    else
      AccessCondition.generateLeaseCondition(leaseId)

    try {
      file.uploadText(content, "UTF-8", accessCondition, null, new OperationContext)
      if (leaseId != "") file.releaseLease(accessCondition)
    } catch {
      // @todo manage exceptions
      case e: StorageException ⇒ {
        println(e)
        throw e
      }
      case e: IOException      ⇒ {
        println(e)
        throw e
      }
      case e: Exception        ⇒ {
        println(e)
        throw e
      }
    }
  }

  private[this] def filename(partition: Int): String = "partition-" + partition
}
