// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.filters

import com.microsoft.azure.iot.iothubreact.MessageFromDevice

object Device {
  def apply(deviceId: String)(m: MessageFromDevice) = new Device(deviceId).only(m)
}

/** Filter by device ID
  *
  * @param deviceId Device ID
  */
class Device(val deviceId: String) {
  def only(m: MessageFromDevice): Boolean = m.deviceId == deviceId
}
