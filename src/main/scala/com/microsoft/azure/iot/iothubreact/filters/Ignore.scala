// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.filters

import com.microsoft.azure.iot.iothubreact.MessageFromDevice

/** Set of filters to ignore IoT traffic
  *
  */
private[iothubreact] object Ignore {

  /** Ignore the keep alive signal injected by MessageFromDeviceSource
    *
    * @return True if the message must be processed
    */
  def keepAlive = (m: MessageFromDevice) ⇒ !m.isKeepAlive
}
