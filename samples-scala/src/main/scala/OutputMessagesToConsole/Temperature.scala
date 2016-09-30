// Copyright (c) Microsoft. All rights reserved.

package OutputMessagesToConsole

import java.time.{ZoneId, ZonedDateTime}

/** Temperature measure by a device
  *
  * @param deviceId Device ID passed by the device (ideally this matches the ID registered in IoTHub)
  * @param value    Temperature value measured by the device
  * @param time     Time (as a string) when the device measured the temperature
  */
class Temperature(var deviceId: String,
    val value: Float,
    val time: String) {

  // ISO8601 with and without milliseconds decimals

  private val pattern1 = """(\d+)-(\d+)-(\d+)T(\d+):(\d+):(\d+).(\d+)Z""".r
  private val pattern2 = """(\d+)-(\d+)-(\d+)T(\d+):(\d+):(\d+)Z""".r

  /** Parse the time sent by the device.
    *
    * @return the time as an object
    */
  def getTime(): ZonedDateTime = {
    time match {
      case pattern1(y, m, d, h, i, s, n) ⇒ ZonedDateTime.of(y.toInt, m.toInt, d.toInt, h.toInt, i.toInt, s.toInt, n.toInt * 1000000, ZoneId.of("UTC"))
      case pattern2(y, m, d, h, i, s)    ⇒ ZonedDateTime.of(y.toInt, m.toInt, d.toInt, h.toInt, i.toInt, s.toInt, 0, ZoneId.of("UTC"))
      case null                          ⇒ null
      case _                             ⇒ throw new Exception(s"wrong date time format: $time")
    }
  }
}
