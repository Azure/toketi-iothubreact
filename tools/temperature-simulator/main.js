// Copyright (c) Microsoft. All rights reserved.

/*

  How to prepare the devices:

  1. Setup an Azure IoT hub and get the "iothubowner" connection string

  2. Download iothub-explorer.js from

        https://github.com/Azure/azure-iot-sdks/tree/master/tools/iothub-explorer

  3. > npm install
  4. > iothub-explorer.js login "conn string"
  5. > for i in {1000..1010}; do node iothub-explorer.js create device$i --display="deviceId"; done
  6. > node iothub-explorer.js list --display="deviceId,authentication.SymmetricKey.primaryKey" --raw|sort

*/

'use strict';

var frequency   = 1000
var randomness  = 10

var hub_name    = 'my-iothub'

var hub_devices = [
  [{"deviceId":"device1000","authentication":{"SymmetricKey":{"primaryKey":"............"}}}],
  [{"deviceId":"device1001","authentication":{"SymmetricKey":{"primaryKey":"............"}}}],
  [{"deviceId":"device1002","authentication":{"SymmetricKey":{"primaryKey":"............"}}}],
  [{"deviceId":"device1003","authentication":{"SymmetricKey":{"primaryKey":"............"}}}],
  [{"deviceId":"device1004","authentication":{"SymmetricKey":{"primaryKey":"............"}}}],
  [{"deviceId":"device1005","authentication":{"SymmetricKey":{"primaryKey":"............"}}}],
  [{"deviceId":"device1006","authentication":{"SymmetricKey":{"primaryKey":"............"}}}],
  [{"deviceId":"device1007","authentication":{"SymmetricKey":{"primaryKey":"............"}}}],
  [{"deviceId":"device1008","authentication":{"SymmetricKey":{"primaryKey":"............"}}}],
  [{"deviceId":"device1009","authentication":{"SymmetricKey":{"primaryKey":"............"}}}],
  [{"deviceId":"device1010","authentication":{"SymmetricKey":{"primaryKey":"............"}}}]
]

var TemperatureSimulator = require('./device.js')
var Protocol = require('azure-iot-device-amqp').Amqp
var devices = []
for (var i = 0; i < hub_devices.length ; i++) {
  var deviceId = hub_devices[i][0].deviceId
  var accessKey = hub_devices[i][0].authentication.SymmetricKey.primaryKey
  var connectionString = 'HostName='+hub_name+'.azure-devices.net;DeviceId='+deviceId+';SharedAccessKey='+accessKey
  devices[i] = new TemperatureSimulator(deviceId, connectionString, Protocol, frequency + Math.floor(Math.random() * randomness) - randomness/2)
  devices[i].connect()
  devices[i].startSending()
}
