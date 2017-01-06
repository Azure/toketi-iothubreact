// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.checkpointing.backends.cassandra.lib

import com.datastax.driver.core.Session
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._

import scala.collection.JavaConverters._
import scala.language.{implicitConversions, postfixOps}

/** CQL table methods
  *
  * @param session   Cassandra session
  * @param keyspace  Key space
  * @param tableName Table name
  * @tparam T Record type
  */
private[iothubreact] case class Table[T <: ToCassandra](session: Session, keyspace: String, tableName: String) {

  /** Insert a record (upsert)
    *
    * @param record Record data
    */
  def insertRow(record: T) = {
    session.execute(s"INSERT INTO $keyspace.$tableName JSON '${record.toJsonValues}'")
  }

  /** Update a record (upsert)
    *
    * @param record Record data
    */
  def updateRow(record: T): Unit = {
    session.execute(s"INSERT INTO $keyspace.$tableName JSON '${record.toJsonValues}'")
  }

  /** Retrieve a record
    *
    * @param condition CQL condition
    *
    * @return a record as string
    */
  def select(condition: String): JObject = {

    // TODO: return a T object

    val row = session.execute(s"SELECT * FROM $keyspace.$tableName WHERE ${condition}").one()

    var partition = -1
    var offset = ""

    if (row != null) {
      row.getColumnDefinitions.asScala.foreach(definition ⇒ {
        val fieldName = definition.getName
        if (fieldName == "partition") partition = row.getInt(fieldName)
        if (fieldName == "offset") offset = row.getString(fieldName)
      })
    }

    ("partition" -> partition) ~ ("offset" -> offset)
  }
}
