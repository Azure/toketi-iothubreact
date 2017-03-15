// Copyright (c) Microsoft. All rights reserved.

name := "iothub-react"
organization := "com.microsoft.azure.iot"

//version := "0.9.0"
version := "0.9.0-DEV.170313a"

scalaVersion := "2.12.1"
crossScalaVersions := Seq("2.11.8", "2.12.1")

libraryDependencies <++= (scalaVersion) {
  scalaVersion ⇒

    val json4sVersion = "3.5.0"

    Seq(
      // https://github.com/Azure/azure-iot-sdk-java/releases
      "com.microsoft.azure.sdk.iot" % "iot-service-client" % "1.1.15",

      // https://github.com/Azure/azure-event-hubs-java/releases
      "com.microsoft.azure" % "azure-eventhubs" % "0.11.0",

      // https://github.com/Azure/azure-storage-java/releases
      "com.microsoft.azure" % "azure-storage" % "5.0.0",

      // https://github.com/datastax/java-driver/releases
      "com.datastax.cassandra" % "cassandra-driver-core" % "3.1.4",

      // https://github.com/akka/akka/releases
      "com.typesafe.akka" %% "akka-stream" % "2.4.17",

      // https://github.com/json4s/json4s/releases
      "org.json4s" %% "json4s-native" % json4sVersion,
      "org.json4s" %% "json4s-jackson" % json4sVersion
    )
}

// Test dependencies
libraryDependencies <++= (scalaVersion) {
  scalaVersion ⇒
    Seq(
      // https://github.com/scalatest/scalatest/releases
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",

      // https://github.com/Azure/azure-iot-sdk-java/releases
      "com.microsoft.azure.sdk.iot" % "iot-device-client" % "1.0.21" % "test"
    )
}

lazy val iotHubReact = project.in(file(".")).configs(IntegrationTest)
lazy val samplesScala = project.in(file("samples-scala")).dependsOn(iotHubReact)
lazy val samplesJava = project.in(file("samples-java")).dependsOn(iotHubReact)

/* Publishing options
 * see http://www.scala-sbt.org/0.13/docs/Artifacts.html
 */
publishArtifact in Test := true
publishArtifact in(Compile, packageDoc) := true
publishArtifact in(Compile, packageSrc) := true
publishArtifact in(Compile, packageBin) := true

// Note: for Bintray, unpublish using SBT
licenses += ("MIT", url("https://github.com/Azure/toketi-iothubreact/blob/master/LICENSE"))
publishMavenStyle := true

// Bintray: Organization > Repository > Package > Version
bintrayOrganization := Some("microsoftazuretoketi")
bintrayRepository := "toketi-repo"
bintrayPackage := "iothub-react"
bintrayReleaseOnPublish in ThisBuild := true

// Required in Sonatype
pomExtra :=
  <url>https://github.com/Azure/toketi-iothubreact</url>
    <scm>
      <url>https://github.com/Azure/toketi-iothubreact</url>
    </scm>
    <developers>
      <developer>
        <id>microsoft</id> <name>Microsoft</name>
      </developer>
    </developers>

/** Miscs
  */
logLevel := Level.Info // Debug|Info|Warn|Error - `Info` provides a better output with `sbt test`.
scalacOptions ++= Seq("-deprecation", "-explaintypes", "-unchecked", "-feature")
showTiming := true
fork := true
parallelExecution := true
