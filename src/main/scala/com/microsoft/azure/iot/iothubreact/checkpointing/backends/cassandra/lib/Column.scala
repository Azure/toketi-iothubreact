// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.checkpointing.backends.cassandra.lib

import com.microsoft.azure.iot.iothubreact.checkpointing.backends.cassandra.lib.ColumnType.ColumnType

/** Column types supported
  */
private[iothubreact] object ColumnType extends Enumeration {
  type ColumnType = Value
  val String, Timestamp, Double, Int = Value

  /** Map type to string used in CQL
    *
    * @param columnType Column type
    *
    * @return Type as string
    */
  def toString(columnType: ColumnType): String = columnType match {
    case ColumnType.String    ⇒ "text"
    case ColumnType.Timestamp ⇒ "timestamp"
    case ColumnType.Double    ⇒ "double"
    case ColumnType.Int       ⇒ "int"

    case _ ⇒ throw new RuntimeException(s"Missing mapping for Cassandra type ${columnType}")
  }

  /** Parse name to enum
    *
    * @param typeAsString Type as string
    *
    * @return Column type
    */
  def fromName(typeAsString: String): ColumnType = typeAsString match {
    case "text"      ⇒ ColumnType.String
    case "timestamp" ⇒ ColumnType.Timestamp
    case "double"    ⇒ ColumnType.Double
    case "int"       ⇒ ColumnType.Int

    case _ ⇒ throw new IllegalArgumentException(s"Unknown Cassandra column type '${typeAsString}'")
  }
}

/** Create a column instance
  *
  * @param name   Name of the column
  * @param `type` Type of the column
  * @param index  Whether to the column value is part of the primary key
  */
private[iothubreact] case class Column(val name: String, val `type`: ColumnType, val index: Boolean = false)
