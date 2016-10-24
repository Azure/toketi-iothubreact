// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.filters

import com.microsoft.azure.iot.iothubreact.IoTMessage

object Schema {
  def apply(schema: String)(m: IoTMessage) = new Schema(schema).only(m)
}

class Schema(val schema: String) {
  def only(m: IoTMessage): Boolean = m.schema == schema
}
