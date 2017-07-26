// Copyright (c) Microsoft. All rights reserved.

name := "iothub-react"
organization := "com.microsoft.azure.iot"

//version := "0.10.0"

//https://bintray.com/microsoftazuretoketi/toketi-repo/iothub-react
version := "0.10.0-DEV.170725b"

scalaVersion := "2.12.2"
crossScalaVersions := Seq("2.11.11", "2.12.2")

libraryDependencies ++= {
  Seq(
    // https://github.com/Azure/azure-iot-sdk-java/releases
    "com.microsoft.azure.sdk.iot" % "iot-service-client" % "1.6.23",

    // https://github.com/Azure/azure-event-hubs-java/releases
    "com.microsoft.azure" % "azure-eventhubs" % "0.14.3",

    // https://github.com/Azure/azure-storage-java/releases
    "com.microsoft.azure" % "azure-storage" % "5.4.0",

    // https://github.com/Azure/azure-documentdb-java/releases
    "com.microsoft.azure" % "azure-documentdb" % "1.12.0",

    // https://github.com/datastax/java-driver/releases
    "com.datastax.cassandra" % "cassandra-driver-core" % "3.3.0",

    // https://github.com/akka/akka/releases
    "com.typesafe.akka" %% "akka-stream" % "2.5.3",

    // https://github.com/json4s/json4s/releases
    "org.json4s" %% "json4s-native" % "3.5.2",
    "org.json4s" %% "json4s-jackson" % "3.5.2"
  )
}

// Test dependencies
libraryDependencies ++= Seq(
  // https://github.com/scalatest/scalatest/releases
  "org.scalatest" %% "scalatest" % "3.2.0-SNAP7" % "test",

  // https://github.com/Azure/azure-iot-sdk-java/releases
  "com.microsoft.azure.sdk.iot" % "iot-device-client" % "1.3.31" % "test",

  // http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.mockito%22%20AND%20a%3A%22mockito-all%22
  "org.mockito" % "mockito-all" % "1.10.19" % "test"
)

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

// Assembly
assemblyMergeStrategy in assembly := {
  case m if m.startsWith("META-INF") ⇒ MergeStrategy.discard
  case m if m.contains(".txt")       ⇒ MergeStrategy.discard
  case x                             ⇒ (assemblyMergeStrategy in assembly).value(x)
}

/** Miscs
  */
logLevel := Level.Info // Debug|Info|Warn|Error - `Info` provides a better output with `sbt test`.
scalacOptions ++= Seq("-deprecation", "-explaintypes", "-unchecked", "-feature")
showTiming := true
fork := true
parallelExecution := true
