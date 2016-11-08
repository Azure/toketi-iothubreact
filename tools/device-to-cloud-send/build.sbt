// Copyright (c) Microsoft. All rights reserved.

name := "device-to-cloud-send"
organization := "com.microsoft.azure.iot"
version := "0.1.0"

scalaVersion := "2.12.0"

libraryDependencies += "com.typesafe" % "config" % "1.3.0"
libraryDependencies += "com.github.scopt" %% "scopt" % "3.5.0"
libraryDependencies += "com.microsoft.azure.iothub-java-client" % "iothub-java-device-client" % "1.0.14"

/** Miscs
  */
logLevel := Level.Warn // Debug|Info|Warn|Error
scalacOptions ++= Seq("-deprecation", "-explaintypes", "-unchecked", "-feature")
