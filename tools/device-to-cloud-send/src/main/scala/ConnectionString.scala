// Copyright (c) Microsoft. All rights reserved.

/* Format a connection string accordingly to SDK */
object ConnectionString {

  /** Format a connection string accordingly to SDK
    *
    * @param hubName   IoT hub name
    * @param deviceId  Device ID
    * @param accessKey Device authorization key
    *
    * @return A connection string
    */
  def build(hubName: String, deviceId: String, accessKey: String): String = {
    s"HostName=$hubName.azure-devices.net;DeviceId=$deviceId;SharedAccessKey=$accessKey"
  }
}
