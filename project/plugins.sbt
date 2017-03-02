resolvers ++= Seq(
  "Sonatype OSS Release Repository" at "https://oss.sonatype.org/content/repositories/releases/"
)
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.4")
addSbtPlugin("io.gatling" % "gatling-sbt" % "2.2.0")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")
addSbtPlugin("com.chatwork" % "sbt-aws-s3-resolver" % "1.0.30")
