// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.filters

import com.microsoft.azure.iot.iothubreact.MessageFromDevice

object Model {
  def apply(model: String)(m: MessageFromDevice) = new Model(model).only(m)
}

/** Filter by model name
  *
  * @param model Model name
  */
class Model(val model: String) {
  def only(m: MessageFromDevice): Boolean = m.model == model
}






