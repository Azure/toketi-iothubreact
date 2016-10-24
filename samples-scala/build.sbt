// Copyright (c) Microsoft. All rights reserved.

scalaVersion := "2.11.8"
crossScalaVersions := Seq("2.11.8", "2.12.0-RC1")

scalacOptions ++= Seq("-deprecation", "-explaintypes", "-unchecked", "-feature")

// This repository contains development snapshots. Production releases are in Maven Central.
resolvers += "Dev Snapshots" at "https://dl.bintray.com/microsoftazuretoketi/toketi-repo"

libraryDependencies ++= {
  val prodVersion = "0.6.0"
  val devVersion = "0.7.0-DEV.161024B"

  Seq(
    "com.microsoft.azure.iot" %% "iothub-react" % devVersion,

    // Jackson libraries for JSON marshalling and unmarshalling
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.3",

    // Jackson module for scala object marshalling and unmarshalling
    "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.2"
  )
}
