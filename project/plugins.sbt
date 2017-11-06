// Resolvers

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

// The Typesafe repository
resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

// The Typesafe snapshots repository
resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

resolvers += Resolver.typesafeRepo("releases")

resolvers += Classpaths.sbtPluginReleases

// scoverage repo on Bintray
resolvers += Resolver.url("scoverage-bintray", url("https://dl.bintray.com/sksamuel/sbt-plugins/"))(Resolver.ivyStylePatterns)

// Plugins

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.7")


addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.1.0")

addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "1.3.4")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.5.4")
