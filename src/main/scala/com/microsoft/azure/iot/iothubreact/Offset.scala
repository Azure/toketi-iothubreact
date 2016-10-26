// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

object Offset {
  def apply(value: String) = new Offset(value)
}

/** Class used to pass the starting point to IoTHub storage
  *
  * @param value The offset value
  */
class Offset(val value: String) {

}
