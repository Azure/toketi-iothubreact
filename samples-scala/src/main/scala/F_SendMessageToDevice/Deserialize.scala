// Copyright (c) Microsoft. All rights reserved.

package F_SendMessageToDevice

import com.microsoft.azure.iot.iothubreact.MessageFromDevice
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse

trait Deserialize {
  def deserialize(m: MessageFromDevice): Temperature = {
    implicit val formats = DefaultFormats
    val temperature = parse(m.contentAsString).extract[Temperature]
    temperature.deviceId = m.deviceId
    temperature
  }
}
