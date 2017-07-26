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

  // 2031-12-31T00:59:59.123Z - 2031-12-31T00:59:59.123+00:00 - 2031-12-31T00:59:59.123+0000 - 2031-12-31T00:59:59.123+00
  private lazy val pattern1: Regex = """(\d{4})-(\d{1,2})-(\d{1,2})T(\d{1,2}):(\d{1,2}):(\d{1,2}).(\d{1,3})(Z|\+00:00|\+0000|\+00)""".r

  // 2031-12-31T00:59:59Z - 2031-12-31T00:59:59+00:00 - 2031-12-31T00:59:59+0000 - 2031-12-31T00:59:59+00
  private lazy val pattern2: Regex = """(\d{4})-(\d{1,2})-(\d{1,2})T(\d{1,2}):(\d{1,2}):(\d{1,2})(Z|\+00:00|\+0000|\+00)""".r

  // 2031-12-31
  private lazy val pattern3: Regex = """(\d{4})-(\d{1,2})-(\d{1,2})""".r

  private lazy val format: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  private lazy val zone  : ZoneId            = ZoneId.of("UTC")

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

      case _ ⇒ throw new RuntimeException(s"Invalid date time format: $text. The date must be a valid ISO8601 date with UTC timezone.")
    }
  }

  override def toString: String = if (zonedDateTime == null) "" else zonedDateTime.format(format)
}
