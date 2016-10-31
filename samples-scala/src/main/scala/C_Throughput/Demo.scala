// Copyright (c) Microsoft. All rights reserved.

package C_Throughput

import akka.stream.scaladsl.Sink
import com.microsoft.azure.iot.iothubreact.IoTMessage
import com.microsoft.azure.iot.iothubreact.ResumeOnError._
import com.microsoft.azure.iot.iothubreact.scaladsl._

import scala.concurrent.duration._
import scala.language.postfixOps

/** Measure the streaming throughput
  */
object Demo extends App {

  val showStatsEvery = 1 second

  // Messages throughput monitoring sink
  val monitor = Sink.foreach[IoTMessage] {
    m ⇒ {
      Monitoring.total += 1
      Monitoring.totals(m.partition.get) += 1
    }
  }

  // Start processing the stream
  IoTHub().source
    .to(monitor)
    .run()

  // Print statistics at some interval
  Monitoring.printStatisticsWithFrequency(showStatsEvery)
}
