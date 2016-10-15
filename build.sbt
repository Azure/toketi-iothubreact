// Copyright (c) Microsoft. All rights reserved.

name := "iothub-react"
organization := "com.microsoft.azure.iot"
version := "0.6.0"

scalaVersion := "2.11.8"
crossScalaVersions := Seq("2.11.8", "2.12.0-RC1")

logLevel := Level.Warn // Debug|Info|Warn|Error
scalacOptions ++= Seq("-deprecation", "-explaintypes", "-unchecked", "-feature")

libraryDependencies <++= (scalaVersion) {
  scalaVersion ⇒
    val azureEventHubSDKVersion = "0.8.2"
    val iothubClientVersion = "1.0.14"
    val scalaTestVersion = "3.0.0"
    val jacksonVersion = "2.8.3"
    val akkaStreamVersion = "2.4.11"

    Seq(
      // Library dependencies
      "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
      "com.microsoft.azure" % "azure-eventhubs" % azureEventHubSDKVersion,

      // Tests dependencies
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
      "com.microsoft.azure.iothub-java-client" % "iothub-java-device-client" % iothubClientVersion % "test",

      // Remove < % "test" > to run samples-scala against the local workspace
      "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion % "test",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion % "test"
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
