// Copyright (c) Microsoft. All rights reserved.

package C_Throughput

import com.typesafe.config.ConfigFactory

import scala.collection.parallel.mutable.ParArray
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps

/** Monitoring logic, some properties to keep count and a method to print the
  * statistics. This demo uses also the information about the last message in
  * each partition, to show how many messages are left to process.
  *
  * Note: for readability the monitoring Sink is in the Demo class
  */
object Monitoring {

  // Auxiliary vars
  private[this] val iotHubPartitions    = ConfigFactory.load().getInt("iothub-react.connection.hubPartitions")
  private[this] var previousTime : Long = 0
  private[this] var previousTotal: Long = 0

  // Total count of messages
  var total: Long = 0

  // Total per partition
  var totals = new ParArray[Long](iotHubPartitions)

  // Remaining messages per partition
  var remain = new ParArray[Long](iotHubPartitions)

  /* Schedule the stats to be printed with some frequency */
  def printStatisticsWithFrequency(frequency: FiniteDuration): Unit = {
    implicit val system = akka.actor.ActorSystem("system")
    system.scheduler.schedule(1 seconds, frequency)(printStats)
  }

  /** Print the number of messages received from each partition, the total
    * and the throughput msg/sec
    */
  private[this] def printStats(): Unit = {

    val now = java.time.Instant.now.toEpochMilli

    if (total > 0 && previousTime > 0) {

      print(s"Partitions count: ")
      for (i ← 0 until iotHubPartitions - 1) print(pad6(totals(i)) + ",")
      print(pad6(totals(iotHubPartitions - 1)))

      val throughput = ((total - previousTotal) * 1000 / (now - previousTime)).toInt
      println(s" - Total: ${pad6(total)} - Speed: $throughput/sec")

      print(s"       remaining: ")
      for (i ← 0 until iotHubPartitions - 1) print(pad6(remain(i)) + ",")
      println(pad6(remain(iotHubPartitions - 1)))
    }

    previousTotal = total
    previousTime = now
  }

  private[this] def pad6(x: Long): String = f"${x}%06d"
}
