// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.filters

import com.microsoft.azure.iot.iothubreact.IoTMessage

/** Set of filters to ignore IoT traffic
  *
  */
private[iothubreact] object Ignore {

  /** Ignore the keep alive signal injected by IoTMessageSource
    *
    * @return True if the message must be processed
    */
  def keepAlive = (m: IoTMessage) ⇒ !m.isKeepAlive
}
