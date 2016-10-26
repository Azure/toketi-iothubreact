// Copyright (c) Microsoft. All rights reserved.

package A_OutputMessagesToConsole

import java.time.{ZoneId, ZonedDateTime}

/** ISO8601 with and without milliseconds decimals
  *
  * @param text String date
  */
case class ISO8601DateTime(text: String) {

  private val pattern1 = """(\d+)-(\d+)-(\d+)T(\d+):(\d+):(\d+).(\d+)Z""".r
  private val pattern2 = """(\d+)-(\d+)-(\d+)T(\d+):(\d+):(\d+)Z""".r

  val value: ZonedDateTime = {
    text match {
      case pattern1(y, m, d, h, i, s, n) ⇒ ZonedDateTime.of(y.toInt, m.toInt, d.toInt, h.toInt, i.toInt, s.toInt, n.toInt * 1000000, ZoneId.of("UTC"))
      case pattern2(y, m, d, h, i, s)    ⇒ ZonedDateTime.of(y.toInt, m.toInt, d.toInt, h.toInt, i.toInt, s.toInt, 0, ZoneId.of("UTC"))
      case null                          ⇒ null
      case _                             ⇒ throw new Exception(s"wrong date time format: $text")
    }
  }

  override def toString: String = if (value == null) "" else value.toString
}
