// Copyright (c) Microsoft. All rights reserved.

scalaVersion := "2.12.0"
crossScalaVersions := Seq("2.11.8", "2.12.0")

scalacOptions ++= Seq("-deprecation", "-explaintypes", "-unchecked", "-feature")

// This repository contains development snapshots. Production releases are in Maven Central.
resolvers += "Dev Snapshots" at "https://dl.bintray.com/microsoftazuretoketi/toketi-repo"

libraryDependencies ++= {
  val prodVersion = "0.8.0"
  val devVersion = "0.8.1-DEV.170309a"

  Seq(
    "com.microsoft.azure.iot" %% "iothub-react" % devVersion
  )
}
