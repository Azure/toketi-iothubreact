// Copyright (c) Microsoft. All rights reserved.

package it.helpers

/** Model used to deserialize the device credentials
  *
  * @param deviceId   Device ID
  * @param primaryKey Device authoriazion key
  */
class DeviceCredentials(val deviceId: String, val primaryKey: String) {}
