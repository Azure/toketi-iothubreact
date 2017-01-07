// Copyright (c) Microsoft. All rights reserved.

package D_Throttling

import akka.stream.ThrottleMode
import akka.stream.scaladsl.{Flow, Sink}
import com.microsoft.azure.iot.iothubreact.MessageFromDevice
import com.microsoft.azure.iot.iothubreact.scaladsl._
import com.microsoft.azure.iot.iothubreact.ResumeOnError._

import scala.concurrent.duration._
import scala.language.postfixOps

object Demo extends App {

  val maxSpeed = 100

  // Sink combining throttling and monitoring
  lazy val throttleAndMonitor = Flow[MessageFromDevice]
    .alsoTo(throttler)
    .to(monitor)

  // Stream throttling sink
  val throttler = Flow[MessageFromDevice]
    .throttle(maxSpeed, 1.second, maxSpeed / 10, ThrottleMode.Shaping)
    .to(Sink.ignore)

  // Messages throughput monitoring sink
  val monitor = Sink.foreach[MessageFromDevice] {
    m ⇒ {
      Monitoring.total += 1
      Monitoring.totals(m.partition.get) += 1
    }
  }

  println(s"Streaming messages at ${maxSpeed} msg/sec")

  IoTHub().source
    .to(throttleAndMonitor)
    .run()

  // Print statistics at some interval
  Monitoring.printStatisticsWithFrequency(1 second)
}
