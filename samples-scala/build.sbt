// Copyright (c) Microsoft. All rights reserved.

scalaVersion := "2.11.8"

// This repository contains development snapshots. Production releases are in Maven Central.
resolvers += "Toketi Dev Snapshots" at "https://dl.bintray.com/microsoftazuretoketi/toketi-repo"

libraryDependencies ++= {
  val prodVersion = "0.6.0"
  val devVersion = "0.6.0-dev"

  Seq(
    "com.microsoft.azure.iot" %% "iothub-react" % prodVersion,

    // Jackson libraries for JSON marshalling and unmarshalling
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.3",

    // Jackson module for scala object marshalling and unmarshalling
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.2"
  )
}
