// Copyright (c) Microsoft. All rights reserved.

name := "iothub-react"
organization := "com.microsoft.azure.iot"
version := "0.8.0-DEV.161031a"

scalaVersion := "2.12.0"
crossScalaVersions := Seq("2.11.8", "2.12.0")

logLevel := Level.Warn // Debug|Info|Warn|Error
scalacOptions ++= Seq("-deprecation", "-explaintypes", "-unchecked", "-feature")

libraryDependencies <++= (scalaVersion) {
  scalaVersion ⇒
    val azureEventHubSDKVersion = "0.8.2"
    val azureStorageSDKVersion = "4.4.0"
    val iothubClientVersion = "1.0.14"
    val scalaTestVersion = "3.0.0"
    val jacksonVersion = "2.8.3"
    val datastaxDriverVersion = "3.1.1"
    val json4sVersion = "3.4.2"

    Seq(
      // Library dependencies
      "com.typesafe.akka" % akkaStreamPackage(scalaVersion) % akkaStreamVersion(scalaVersion),
      "com.microsoft.azure" % "azure-eventhubs" % azureEventHubSDKVersion,
      "com.microsoft.azure" % "azure-storage" % azureStorageSDKVersion,
      "com.datastax.cassandra" % "cassandra-driver-core" % datastaxDriverVersion,
      "org.json4s" % json4sNativePackage(scalaVersion) % json4sVersion,
      "org.json4s" % json4sJacksonPackage(scalaVersion) % json4sVersion,

      // Tests dependencies
      "org.scalatest" % scalaTestPackage(scalaVersion) % scalaTestVersion % "test",
      "com.microsoft.azure.iothub-java-client" % "iothub-java-device-client" % iothubClientVersion % "test",

      // Remove < % "test" > to run samples-java against the local workspace
      // @todo use json4s
      "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion % "test",
      "com.fasterxml.jackson.module" % jacksonModuleScalaPackage(scalaVersion) % jacksonVersion % "test"
    )
}

def akkaStreamPackage(scalaVersion: String): String = scalaVersion match {
  case v if v startsWith "2.11" => "akka-stream_2.11"
  case v if v startsWith "2.12" => "akka-stream_2.12.0-RC2"
}

def akkaStreamVersion(scalaVersion: String): String = scalaVersion match {
  case v if v startsWith "2.11" => "2.4.12"
  case v if v startsWith "2.12" => "2.4.11"
}

def json4sNativePackage(scalaVersion: String): String = scalaVersion match {
  case v if v startsWith "2.11" => "json4s-native_2.11"
  case v if v startsWith "2.12" => "json4s-native_2.12.0-RC2"
}

def json4sJacksonPackage(scalaVersion: String): String = scalaVersion match {
  case v if v startsWith "2.11" => "json4s-jackson_2.11"
  case v if v startsWith "2.12" => "json4s-jackson_2.12.0-RC2"
}

def jacksonModuleScalaPackage(scalaVersion: String): String = scalaVersion match {
  case v if v startsWith "2.11" => "jackson-module-scala_2.11"
  case v if v startsWith "2.12" => "jackson-module-scala_2.12.0-RC1"
}

def scalaTestPackage(scalaVersion: String): String = scalaVersion match {
  case v if v startsWith "2.11" => "scalatest_2.11"
  case v if v startsWith "2.12" => "scalatest_2.12.0-RC2"
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
