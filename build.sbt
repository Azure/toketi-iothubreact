// Copyright (c) Microsoft. All rights reserved.

name := "iothub-react"
organization := "com.microsoft.azure.iot"
version := "0.8.0-DEV.161101a"

scalaVersion := "2.12.0"
crossScalaVersions := Seq("2.11.8", "2.12.0")

libraryDependencies <++= (scalaVersion) {
  scalaVersion ⇒
    val azureEventHubSDKVersion = "0.8.2"
    val azureStorageSDKVersion = "4.4.0"
    val iothubDeviceClientVersion = "1.0.14"
    val iothubServiceClientVersion = "1.0.10"
    val scalaTestVersion = "3.0.0"
    val jacksonVersion = "2.8.3"
    val datastaxDriverVersion = "3.1.1"
    val json4sVersion = "3.4.2"

    Seq(
      // Library dependencies
      "com.microsoft.azure.iothub-java-client" % "iothub-java-service-client" % iothubServiceClientVersion,
      "com.microsoft.azure" % "azure-eventhubs" % azureEventHubSDKVersion,
      "com.microsoft.azure" % "azure-storage" % azureStorageSDKVersion,
      "com.datastax.cassandra" % "cassandra-driver-core" % datastaxDriverVersion,
      "com.typesafe.akka" % pkg("akka-stream", scalaVersion) % akkaStreamVersion(scalaVersion),
      "org.json4s" % pkg("json4s-native", scalaVersion) % json4sVersion,
      "org.json4s" % pkg("json4s-jackson", scalaVersion) % json4sVersion,

      // Tests dependencies
      "org.scalatest" % pkg("scalatest", scalaVersion) % scalaTestVersion % "test",
      "com.microsoft.azure.iothub-java-client" % "iothub-java-device-client" % iothubDeviceClientVersion % "test",

      // Remove < % "test" > to run samples-java against the local workspace
      // @todo use json4s
      "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion % "test",
      "com.fasterxml.jackson.module" % pkg("jackson-module-scala", scalaVersion) % jacksonVersion % "test"
    )
}

def pkg(name: String, scalaVersion: String): String = {
  val version = CrossVersion.partialVersion(scalaVersion)
  name match {
    case "akka-stream"          ⇒ version match {
      case Some((2, 11)) => name + "_2.11"
      case Some((2, 12)) => name + "_2.12.0-RC2"
    }
    case "json4s-native"        ⇒ version match {
      case Some((2, 11)) => name + "_2.11"
      case Some((2, 12)) => name + "_2.12.0-RC2"
    }
    case "json4s-jackson"       ⇒ version match {
      case Some((2, 11)) => name + "_2.11"
      case Some((2, 12)) => name + "_2.12.0-RC2"
    }
    case "jackson-module-scala" ⇒ version match {
      case Some((2, 11)) => name + "_2.11"
      case Some((2, 12)) => name + "_2.12.0-RC1"
    }
    case "scalatest"            ⇒ version match {
      case Some((2, 11)) => name + "_2.11"
      case Some((2, 12)) => name + "_2.12.0-RC2"
    }
  }
}

def akkaStreamVersion(scalaVersion: String): String = CrossVersion.partialVersion(scalaVersion) match {
  case Some((2, 11)) => "2.4.12"
  case Some((2, 12)) => "2.4.11"
      // Temp deps for forked copy of Azure SDK fork
      "com.microsoft.azure.iothub-java-client" % "websocket-transport-layer" % "0.1.0",
      //"org.apache.qpid" % "proton-j" % "0.15.0",
      "commons-codec" % "commons-codec" % "1.6",
      "com.google.code.gson" % "gson" % "2.5",
      "org.glassfish" % "javax.json" % "1.0.4"
}

lazy val root = project.in(file(".")).configs(IntegrationTest)

/** Miscs
  */
logLevel := Level.Warn // Debug|Info|Warn|Error
scalacOptions ++= Seq("-deprecation", "-explaintypes", "-unchecked", "-feature")
showTiming := true
fork := true
parallelExecution := true

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
