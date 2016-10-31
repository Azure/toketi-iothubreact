// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.filters

import com.microsoft.azure.iot.iothubreact.IoTMessage

object Device {
  def apply(deviceId: String)(m: IoTMessage) = new Device(deviceId).only(m)
}

/** Filter by device ID
  *
  * @param deviceId Device ID
  */
class Device(val deviceId: String) {
  def only(m: IoTMessage): Boolean = m.deviceId == deviceId
}
