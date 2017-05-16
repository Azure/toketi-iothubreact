// Copyright (c) Microsoft. All rights reserved.

package F_SendMessageToDevice

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}

import scala.util.matching.Regex

/** ISO8601 with and without milliseconds decimals
  *
  * @param text String date
  */
case class ISO8601DateTime(text: String) {

  private lazy val pattern1: Regex             = """(\d{4})-(\d{1,2})-(\d{1,2})T(\d{1,2}):(\d{1,2}):(\d{1,2}).(\d{1,3})Z""".r
  private lazy val pattern2: Regex             = """(\d{4})-(\d{1,2})-(\d{1,2})T(\d{1,2}):(\d{1,2}):(\d{1,2})Z""".r
  private lazy val pattern3: Regex             = """(\d{4})-(\d{1,2})-(\d{1,2})""".r
  private lazy val format  : DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  private lazy val zone    : ZoneId            = ZoneId.of("UTC")

  private lazy val zonedDateTime: ZonedDateTime = {
    text match {
      case pattern1(y, m, d, h, i, s, n) ⇒
        val ni = n.toInt
        val nanos = if (ni > 99) ni * 1000000 else if (ni > 9) ni * 10000000 else ni * 100000000
        ZonedDateTime.of(y.toInt, m.toInt, d.toInt, h.toInt, i.toInt, s.toInt, nanos, zone)

      case pattern2(y, m, d, h, i, s) ⇒
        ZonedDateTime.of(y.toInt, m.toInt, d.toInt, h.toInt, i.toInt, s.toInt, 0, zone)

      case pattern3(y, m, d) ⇒
        ZonedDateTime.of(y.toInt, m.toInt, d.toInt, 0, 0, 0, 0, zone)

      case null ⇒ null
      case _    ⇒ throw new Exception(s"wrong date time format: $text")
    }
  }

  override def toString: String = if (zonedDateTime == null) "" else zonedDateTime.format(format)
}
