// Resolvers

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

// The Typesafe repository
resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

// The Typesafe snapshots repository
resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

resolvers += Resolver.typesafeRepo("releases")

resolvers += Classpaths.sbtPluginReleases

resolvers += Resolver.bintrayIvyRepo("sbt", "sbt-plugin-releases")


// scoverage repo on Bintray
resolvers += Resolver.url("scoverage-bintray", url("https://dl.bintray.com/sksamuel/sbt-plugins/"))(Resolver.ivyStylePatterns)

// Plugins

addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.14")

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.7")


addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.2")

addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "1.3.11")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.3.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.6.2")

addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.2.1")
