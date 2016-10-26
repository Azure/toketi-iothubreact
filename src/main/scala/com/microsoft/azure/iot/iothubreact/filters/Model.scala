// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.filters

import com.microsoft.azure.iot.iothubreact.IoTMessage

object Model {
  def apply(model: String)(m: IoTMessage) = new Model(model).only(m)
}

class Model(val model: String) {
  def only(m: IoTMessage): Boolean = m.model == model
}
