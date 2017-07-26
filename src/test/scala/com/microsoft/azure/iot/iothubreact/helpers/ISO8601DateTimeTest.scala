// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.helpers

import java.time.ZoneId

import org.scalatest.Matchers._
import org.scalatest._

class ISO8601DateTimeTest extends FeatureSpec {

  info("As an IoT developer")
  info("I use a subset of ISO 8601 datetime UTC format")
  info("And IoTHubReact should understand it")

  Feature("It can parse date and time") {

    val utc = ZoneId.of("UTC")

    Scenario("YYYY-MM-DD") {
      val value = "2018-05-29"
      val p = ISO8601DateTime(value)
      val z = p.zonedDateTime
      val i = p.instant
      val s = p.toString

      z.getZone should equal(utc)
      z.getDayOfMonth should equal(29)
      z.getMonthValue should equal(5)
      z.getYear should equal(2018)
      z.getHour should equal(0)
      z.getMinute should equal(0)
      z.getSecond should equal(0)

      i.getEpochSecond should equal(z.toEpochSecond)

      s should equal("2018-05-29T00:00:00.000Z")
    }

    Scenario("YYYY-MM-DDThh:mm:ssZ") {
      val value = "2018-05-29T13:05:59Z"
      val p = ISO8601DateTime(value)
      val z = p.zonedDateTime
      val i = p.instant
      val s = p.toString

      z.getZone should equal(utc)
      z.getDayOfMonth should equal(29)
      z.getMonthValue should equal(5)
      z.getYear should equal(2018)
      z.getHour should equal(13)
      z.getMinute should equal(5)
      z.getSecond should equal(59)

      i.getEpochSecond should equal(z.toEpochSecond)

      s should equal("2018-05-29T13:05:59.000Z")
    }

    Scenario("YYYY-MM-DDThh:mm:ss+00:00") {
      val value = "2018-05-29T13:05:59+00:00"
      val p = ISO8601DateTime(value)
      val z = p.zonedDateTime
      val i = p.instant
      val s = p.toString

      z.getZone should equal(utc)
      z.getDayOfMonth should equal(29)
      z.getMonthValue should equal(5)
      z.getYear should equal(2018)
      z.getHour should equal(13)
      z.getMinute should equal(5)
      z.getSecond should equal(59)

      i.getEpochSecond should equal(z.toEpochSecond)

      s should equal("2018-05-29T13:05:59.000Z")
    }

    Scenario("YYYY-MM-DDThh:mm:ss+0000") {
      val value = "2018-05-29T13:05:59+0000"
      val p = ISO8601DateTime(value)
      val z = p.zonedDateTime
      val i = p.instant
      val s = p.toString

      z.getZone should equal(utc)
      z.getDayOfMonth should equal(29)
      z.getMonthValue should equal(5)
      z.getYear should equal(2018)
      z.getHour should equal(13)
      z.getMinute should equal(5)
      z.getSecond should equal(59)

      i.getEpochSecond should equal(z.toEpochSecond)

      s should equal("2018-05-29T13:05:59.000Z")
    }

    Scenario("YYYY-MM-DDThh:mm:ss+00") {
      val value = "2018-05-29T13:05:59+00"
      val p = ISO8601DateTime(value)
      val z = p.zonedDateTime
      val i = p.instant
      val s = p.toString

      z.getZone should equal(utc)
      z.getDayOfMonth should equal(29)
      z.getMonthValue should equal(5)
      z.getYear should equal(2018)
      z.getHour should equal(13)
      z.getMinute should equal(5)
      z.getSecond should equal(59)

      i.getEpochSecond should equal(z.toEpochSecond)

      s should equal("2018-05-29T13:05:59.000Z")
    }

    Scenario("YYYY-MM-DDThh:mm:ss.SZ") {
      val value = "2018-05-29T11:05:59.3Z"
      val p = ISO8601DateTime(value)
      val z = p.zonedDateTime
      val i = p.instant
      val s = p.toString

      z.getZone should equal(utc)
      z.getDayOfMonth should equal(29)
      z.getMonthValue should equal(5)
      z.getYear should equal(2018)
      z.getHour should equal(11)
      z.getMinute should equal(5)
      z.getSecond should equal(59)

      i.getEpochSecond should equal(z.toEpochSecond)

      s should equal("2018-05-29T11:05:59.300Z")
    }

    Scenario("YYYY-MM-DDThh:mm:ss.S+00:00") {
      val value = "2018-05-29T11:05:59.3+00:00"
      val p = ISO8601DateTime(value)
      val z = p.zonedDateTime
      val i = p.instant
      val s = p.toString

      z.getZone should equal(utc)
      z.getDayOfMonth should equal(29)
      z.getMonthValue should equal(5)
      z.getYear should equal(2018)
      z.getHour should equal(11)
      z.getMinute should equal(5)
      z.getSecond should equal(59)

      i.getEpochSecond should equal(z.toEpochSecond)

      s should equal("2018-05-29T11:05:59.300Z")
    }

    Scenario("YYYY-MM-DDThh:mm:ss.S+0000") {
      val value = "2018-05-29T11:05:59.3+0000"
      val p = ISO8601DateTime(value)
      val z = p.zonedDateTime
      val i = p.instant
      val s = p.toString

      z.getZone should equal(utc)
      z.getDayOfMonth should equal(29)
      z.getMonthValue should equal(5)
      z.getYear should equal(2018)
      z.getHour should equal(11)
      z.getMinute should equal(5)
      z.getSecond should equal(59)

      i.getEpochSecond should equal(z.toEpochSecond)

      s should equal("2018-05-29T11:05:59.300Z")
    }

    Scenario("YYYY-MM-DDThh:mm:ss.S+00") {
      val value = "2018-05-29T11:05:59.3+00"
      val p = ISO8601DateTime(value)
      val z = p.zonedDateTime
      val i = p.instant
      val s = p.toString

      z.getZone should equal(utc)
      z.getDayOfMonth should equal(29)
      z.getMonthValue should equal(5)
      z.getYear should equal(2018)
      z.getHour should equal(11)
      z.getMinute should equal(5)
      z.getSecond should equal(59)

      i.getEpochSecond should equal(z.toEpochSecond)

      s should equal("2018-05-29T11:05:59.300Z")
    }

    Scenario("YYYY-MM-DDThh:mm:ss.SSZ") {
      val value = "2018-05-29T13:05:59.13Z"
      val p = ISO8601DateTime(value)
      val z = p.zonedDateTime
      val i = p.instant
      val s = p.toString

      z.getZone should equal(utc)
      z.getDayOfMonth should equal(29)
      z.getMonthValue should equal(5)
      z.getYear should equal(2018)
      z.getHour should equal(13)
      z.getMinute should equal(5)
      z.getSecond should equal(59)

      i.getEpochSecond should equal(z.toEpochSecond)

      s should equal("2018-05-29T13:05:59.130Z")
    }

    Scenario("YYYY-MM-DDThh:mm:ss.SS+00:00") {
      val value = "2018-05-29T13:05:59.13+00:00"
      val p = ISO8601DateTime(value)
      val z = p.zonedDateTime
      val i = p.instant
      val s = p.toString

      z.getZone should equal(utc)
      z.getDayOfMonth should equal(29)
      z.getMonthValue should equal(5)
      z.getYear should equal(2018)
      z.getHour should equal(13)
      z.getMinute should equal(5)
      z.getSecond should equal(59)

      i.getEpochSecond should equal(z.toEpochSecond)

      s should equal("2018-05-29T13:05:59.130Z")
    }

    Scenario("YYYY-MM-DDThh:mm:ss.SS+0000") {
      val value = "2018-05-29T13:05:59.13+0000"
      val p = ISO8601DateTime(value)
      val z = p.zonedDateTime
      val i = p.instant
      val s = p.toString

      z.getZone should equal(utc)
      z.getDayOfMonth should equal(29)
      z.getMonthValue should equal(5)
      z.getYear should equal(2018)
      z.getHour should equal(13)
      z.getMinute should equal(5)
      z.getSecond should equal(59)

      i.getEpochSecond should equal(z.toEpochSecond)

      s should equal("2018-05-29T13:05:59.130Z")
    }

    Scenario("YYYY-MM-DDThh:mm:ss.SS+00") {
      val value = "2018-05-29T13:05:59.13+00"
      val p = ISO8601DateTime(value)
      val z = p.zonedDateTime
      val i = p.instant
      val s = p.toString

      z.getZone should equal(utc)
      z.getDayOfMonth should equal(29)
      z.getMonthValue should equal(5)
      z.getYear should equal(2018)
      z.getHour should equal(13)
      z.getMinute should equal(5)
      z.getSecond should equal(59)

      i.getEpochSecond should equal(z.toEpochSecond)

      s should equal("2018-05-29T13:05:59.130Z")
    }

    Scenario("YYYY-MM-DDThh:mm:ss.SSSz") {
      val value = "2018-05-29T13:05:59.135Z"
      val p = ISO8601DateTime(value)
      val z = p.zonedDateTime
      val i = p.instant
      val s = p.toString

      z.getZone should equal(utc)
      z.getDayOfMonth should equal(29)
      z.getMonthValue should equal(5)
      z.getYear should equal(2018)
      z.getHour should equal(13)
      z.getMinute should equal(5)
      z.getSecond should equal(59)

      i.getEpochSecond should equal(z.toEpochSecond)

      s should equal("2018-05-29T13:05:59.135Z")
    }

    Scenario("YYYY-M-DTh:m:s.Sz") {
      val value = "2018-5-2T1:5:9.1Z"
      val p = ISO8601DateTime(value)
      val z = p.zonedDateTime
      val i = p.instant
      val s = p.toString

      z.getZone should equal(utc)
      z.getDayOfMonth should equal(2)
      z.getMonthValue should equal(5)
      z.getYear should equal(2018)
      z.getHour should equal(1)
      z.getMinute should equal(5)
      z.getSecond should equal(9)

      i.getEpochSecond should equal(z.toEpochSecond)

      s should equal("2018-05-02T01:05:09.100Z")
    }

    Scenario("0001-01-01") {
      val value = "0001-01-01"
      val p = ISO8601DateTime(value)
      val z = p.zonedDateTime
      val i = p.instant
      val s = p.toString

      z.getZone should equal(utc)
      z.getDayOfMonth should equal(1)
      z.getMonthValue should equal(1)
      z.getYear should equal(1)
      z.getHour should equal(0)
      z.getMinute should equal(0)
      z.getSecond should equal(0)

      i.getEpochSecond should equal(z.toEpochSecond)

      s should equal("0001-01-01T00:00:00.000Z")
    }
  }
}
