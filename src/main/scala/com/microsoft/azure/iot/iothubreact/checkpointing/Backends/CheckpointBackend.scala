// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.checkpointing.Backends

import com.microsoft.azure.iot.iothubreact.checkpointing.Configuration

private[iothubreact] trait CheckpointBackend {

  def checkpointNamespace: String = "" //Configuration.storageNamespace

  /** Read the offset of the last record processed for the given partition
    *
    * @param partition IoT hub partition number
    *
    * @return Offset of the last record (already) processed
    */
  def readOffset(partition: Int): String

  /** Store the offset for the given IoT hub partition
    *
    * @param partition IoT hub partition number
    * @param offset    IoT hub partition offset
    */
  def writeOffset(partition: Int, offset: String): Unit
}
