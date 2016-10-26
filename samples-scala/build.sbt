// Copyright (c) Microsoft. All rights reserved.

scalaVersion := "2.11.8"
crossScalaVersions := Seq("2.11.8", "2.12.0-RC1")

scalacOptions ++= Seq("-deprecation", "-explaintypes", "-unchecked", "-feature")

// This repository contains development snapshots. Production releases are in Maven Central.
resolvers += "Dev Snapshots" at "https://dl.bintray.com/microsoftazuretoketi/toketi-repo"

libraryDependencies ++= {
  val prodVersion = "0.6.0"
  val devVersion = "0.7.0-DEV.161025c"

  Seq(
    "com.microsoft.azure.iot" %% "iothub-react" % devVersion
  )
}
