resolvers += Resolver.sonatypeRepo("releases")

// Wartremover
// https://github.com/typelevel/wartremover
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.1.1")

// TravisCI
// https://github.com/dwijnand/sbt-travisci
addSbtPlugin("com.dwijnand" % "sbt-travisci" % "1.1.0")

// SBT Header
// https://github.com/sbt/sbt-header
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "2.0.0")
