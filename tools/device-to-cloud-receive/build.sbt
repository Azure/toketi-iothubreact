// Copyright (c) Microsoft. All rights reserved.

name := "device-to-cloud-receive"
organization := "com.microsoft.azure.iot"
version := "0.1.0"

scalaVersion := "2.12.0"
crossScalaVersions := Seq("2.11.8", "2.12.0")

libraryDependencies <++= (scalaVersion) {
  scalaVersion ⇒
    Seq(
      "com.typesafe" % "config" % "1.3.0",
      "com.microsoft.azure" % "azure-eventhubs" % "0.9.0"
    )
}

/** Miscs
  */
logLevel := Level.Debug // Debug|Info|Warn|Error
scalacOptions ++= Seq("-deprecation", "-explaintypes", "-unchecked", "-feature")
showTiming := true
fork := true
parallelExecution := true
licenses += ("MIT", url("https://github.com/Azure/toketi-iothubreact/blob/master/LICENSE"))
