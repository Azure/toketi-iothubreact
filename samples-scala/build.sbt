// Copyright (c) Microsoft. All rights reserved.

scalaVersion := "2.12.1"

libraryDependencies <++= (scalaVersion) {
  scalaVersion ⇒
    Seq(
      "com.microsoft.azure" % "azure-eventhubs-eph" % "0.11.0"
    )
}
