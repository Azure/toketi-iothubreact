// Copyright (c) Microsoft. All rights reserved.

package C_Throughput

import akka.stream.scaladsl.Sink
import com.microsoft.azure.iot.iothubreact.MessageFromDevice
import com.microsoft.azure.iot.iothubreact.ResumeOnError._
import com.microsoft.azure.iot.iothubreact.scaladsl._

import scala.concurrent.duration._
import scala.language.postfixOps

/** Measure the streaming throughput and show how many messages are left
  */
object Demo extends App {

  val showStatsEvery = 1 second

  // Messages throughput monitoring sink
  val monitor = Sink.foreach[MessageFromDevice] {
    m ⇒ {
      Monitoring.total += 1

      val partition = m.partitionInfo.partitionNumber.get
      Monitoring.totals(partition) += 1
      Monitoring.remain(partition) = if (m.partitionInfo.lastSequenceNumber.isEmpty) 0
                                     else m.partitionInfo.lastSequenceNumber.get - m.sequenceNumber
    }
  }

  // Start processing the stream
  IoTHub().source
    .to(monitor)
    .run()

  // Print statistics at some interval
  Monitoring.printStatisticsWithFrequency(showStatsEvery)
}
