// Copyright (c) Microsoft. All rights reserved.

package MessagesThroughput

import akka.stream.ThrottleMode
import akka.stream.scaladsl.{Flow, Sink}
import com.microsoft.azure.iot.iothubreact.IoTMessage
import com.microsoft.azure.iot.iothubreact.scaladsl.IoTHub

import scala.concurrent.duration._
import scala.io.StdIn

/** Retrieve messages from IoT hub managing the stream velocity
  *
  * Demo showing how to:
  * - Measure the streaming throughput
  * - Traffic shaping by throttling the stream speed
  * - How to combine multiple destinations
  * - Back pressure
  */
object Demo extends App with ReactiveStreaming {

  // Maximum speed allowed
  val maxSpeed = 200

  val showStatsEvery = 1 second

  print(s"Do you want to test throttling (${maxSpeed} msg/sec) ? [y/N] ")
  val input      = StdIn.readLine()
  val throttling = input.size > 0 && input(0).toUpper == 'Y'

  // Stream throttling sink
  val throttler = Flow[IoTMessage]
    .throttle(maxSpeed, 1.second, maxSpeed / 10, ThrottleMode.Shaping)
    .to(Sink.ignore)

  // Messages throughput monitoring sink
  val monitor = Sink.foreach[IoTMessage] {
    m ⇒ {
      Monitoring.total += 1
      Monitoring.totals(m.partition.get) += 1
    }
  }

  // Sink combining throttling and monitoring
  val throttleAndMonitor = Flow[IoTMessage]
    .alsoTo(throttler)
    // .alsoTo(...) // Using alsoTo it's possible to deliver to multiple destinations
    .to(monitor)

  // Start processing the stream
  if (throttling) {
    new IoTHub().source
      .to(throttleAndMonitor)
      .run()
  } else {
    new IoTHub().source
      .to(monitor)
      .run()
  }

  // Print statistics at some interval
  Monitoring.printStatisticsWithFrequency(showStatsEvery)
}
