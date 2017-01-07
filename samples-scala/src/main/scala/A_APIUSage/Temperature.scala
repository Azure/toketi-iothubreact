// Copyright (c) Microsoft. All rights reserved.

package A_APIUSage

/** Temperature measure by a device
  *
  * @param value Temperature value measured by the device
  * @param time  Time (as a string) when the device measured the temperature
  */
case class Temperature(value: Float, time: String) {

  var deviceId: String = ""

  val datetime = ISO8601DateTime(time)
}
