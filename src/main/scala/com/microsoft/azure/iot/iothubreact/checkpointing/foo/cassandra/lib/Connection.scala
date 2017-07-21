// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.checkpointing.backends.cassandra.lib

import com.datastax.driver.core.Cluster

/** Cassandra connection
  *
  * @param contactPoint      Hostname (and port, e.g. 1.2.3.4:9042)
  * @param replicationFactor Table replication factor
  * @param table             Table schema
  */
private[iothubreact] case class Connection(
    contactPoint: String,
    replicationFactor: Int,
    auth: Option[Auth],
    table: TableSchema) {

  private lazy val hostPort = extractHostPort()
  private lazy val cluster  = {
    val builder = Cluster.builder().addContactPoint(hostPort._1).withPort(hostPort._2)
    auth map {
      creds ⇒ builder.withCredentials(creds.username, creds.password)
    } getOrElse (builder) build()
  }

  implicit lazy val session = cluster.connect()

  /** Create the key space if not present
    */
  def createKeyspaceIfNotExists(): Unit = {
    val cql = s"CREATE KEYSPACE IF NOT EXISTS ${table.keyspaceCQL}" +
      s" WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor':${replicationFactor}};"
    session.execute(cql)
  }

  /** Create the table if not present
    */
  def createTableIfNotExists(): Unit = {
    createT(table.nameCQL, table.columns)
  }

  /** Get an instance of the table
    *
    * @tparam A Type of the records stored in the table
    *
    * @return Table instance
    */
  def getTable[A <: ToCassandra](): Table[A] = {
    Table[A](session, table.keyspaceCQL, table.nameCQL)
  }

  /** Parse the hostname and extract host + port
    *
    * @return host and port tuple
    */
  private[this] def extractHostPort(): (String, Int) = {
    val tokens = contactPoint.split(":")
    val addr = tokens(0)
    val port = if (tokens.size == 2)
                 tokens(1).toInt
               else
                 9042

    (addr, port)
  }

  /** Generate CQL to create table using column names and index definitions
    *
    * @param tableName Table name
    * @param columns   Columns
    */
  private[this] def createT(tableName: String, columns: Seq[Column]): Unit = {
    val columnsSql = columns.foldLeft("")((b, a) ⇒ s"$b\n${a.name} ${ColumnType.toString(a.`type`)},")
    val indexesSql = columns.filter(_.index).map(_.name).mkString("PRIMARY KEY(", ", ", ")")
    val createTable = s"CREATE TABLE IF NOT EXISTS ${table.keyspaceCQL}.$tableName($columnsSql $indexesSql)"
    session.execute(createTable)
  }
}
