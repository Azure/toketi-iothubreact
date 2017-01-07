// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.scaladsl

import com.microsoft.azure.iot.iothubreact.javadsl.{OffsetList ⇒ JavaOffsetList}

import scala.collection.JavaConverters._

object OffsetList {
  def apply(values: Seq[String]) = new OffsetList(values)

  def apply(values: JavaOffsetList) = new OffsetList(values.values.asScala)
}

/** A list of Offsets (type erasure workaround)
  *
  * @param values The offset value
  */
class OffsetList(val values: Seq[String]) {

}
