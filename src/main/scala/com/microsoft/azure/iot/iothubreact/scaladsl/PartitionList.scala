// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.scaladsl

import com.microsoft.azure.iot.iothubreact.javadsl.{PartitionList ⇒ JavaPartitionList}

import scala.collection.JavaConverters._

object PartitionList {
  def apply(values: Seq[Int]) = new PartitionList(values)

  def apply(values: JavaPartitionList) = new PartitionList(values.values.asScala.map(_.intValue()))
}

/** A list of Partition IDs (type erasure workaround)
  *
  * @param values List of partition IDs
  */
class PartitionList(val values: Seq[Int]) {

}
