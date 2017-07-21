// Copyright (c) Microsoft. All rights reserved.

logLevel := Level.Warn

resolvers += Classpaths.sbtPluginReleases

// publishing dev snapshots to Bintray
// - https://github.com/sbt/sbt-bintray/releases
// old: addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0"
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.1")

// `sbt assembly`
// - https://github.com/sbt/sbt-assembly/releases
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")
