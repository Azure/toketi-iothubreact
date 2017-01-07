// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.filters

import com.microsoft.azure.iot.iothubreact.MessageFromDevice

object MessageType {
  def apply(messageType: String)(m: MessageFromDevice) = new MessageType(messageType).filter(m)
}

/** Filter by message type
  *
  * @param messageType Message type
  */
class MessageType(val messageType: String) {
  def filter(m: MessageFromDevice): Boolean = m.messageType == messageType
}






