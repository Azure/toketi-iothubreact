// Copyright (c) Microsoft. All rights reserved.

name := "iothub-react"
organization := "com.microsoft.azure.iot"

version := "0.8.0"
//version := "0.8.0-DEV.170106a"

scalaVersion := "2.12.1"
crossScalaVersions := Seq("2.11.8", "2.12.1")

libraryDependencies <++= (scalaVersion) {
  scalaVersion ⇒
    val azureEventHubSDKVersion = "0.9.0"
    val azureStorageSDKVersion = "4.4.0"
    val iothubDeviceClientVersion = "1.0.15"
    val iothubServiceClientVersion = "1.0.10"
    val scalaTestVersion = "3.0.1"
    val datastaxDriverVersion = "3.1.1"
    val json4sVersion = "3.5.0"
    val akkaStreamVersion = "2.4.16"

    Seq(
      // Library dependencies
      "com.microsoft.azure.iothub-java-client" % "iothub-java-service-client" % iothubServiceClientVersion,
      "com.microsoft.azure" % "azure-eventhubs" % azureEventHubSDKVersion,
      "com.microsoft.azure" % "azure-storage" % azureStorageSDKVersion,
      "com.datastax.cassandra" % "cassandra-driver-core" % datastaxDriverVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
      "org.json4s" %% "json4s-native" % json4sVersion,
      "org.json4s" %% "json4s-jackson" % json4sVersion,

      // Tests dependencies
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
      "com.microsoft.azure.iothub-java-client" % "iothub-java-device-client" % iothubDeviceClientVersion % "test"
    )
}

lazy val root = project.in(file(".")).configs(IntegrationTest)

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
logLevel := Level.Debug // Debug|Info|Warn|Error
scalacOptions ++= Seq("-deprecation", "-explaintypes", "-unchecked", "-feature")
showTiming := true
fork := true
parallelExecution := true
