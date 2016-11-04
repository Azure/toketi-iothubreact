// Copyright (c) Microsoft. All rights reserved.

package D_Throttling

import akka.stream.ThrottleMode
import akka.stream.scaladsl.{Flow, Sink}
import com.microsoft.azure.iot.iothubreact.MessageFromDevice
import com.microsoft.azure.iot.iothubreact.scaladsl._
import com.microsoft.azure.iot.iothubreact.ResumeOnError._

import scala.concurrent.duration._
import scala.language.postfixOps

/** Retrieve messages from IoT hub managing the stream velocity
  *
  * Demo showing:
  * - Traffic shaping by throttling the stream speed
  * - How to combine multiple destinations
  * - Back pressure
  */
object Demo extends App {

  val maxSpeed       = 50
  val showStatsEvery = 1 second

  println(s"Streaming messages at ${maxSpeed} msg/sec")

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

  // Start processing the stream
  IoTHub().source
    .to(throttleAndMonitor)
    .run()

  // Print statistics at some interval
  Monitoring.printStatisticsWithFrequency(showStatsEvery)
}
