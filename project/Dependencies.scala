import sbt._

object Dependencies {

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1"

  object gatling {
    val versionOfScala211 = "2.2.5"
    val versionOfScala212 = "3.1.3"

    val coreOfScala211 = gatling("core", versionOfScala211)
    val testFrameworkOfScala211 = gatling("test-framework", versionOfScala211)
    val highchartsOfScala211 = "io.gatling.highcharts" % "gatling-charts-highcharts" % versionOfScala211

    val coreOfScala212 = gatling("core", versionOfScala212)
    val testFrameworkOfScala212 = gatling("test-framework", versionOfScala212)
    val highchartsOfScala212 = "io.gatling.highcharts" % "gatling-charts-highcharts" % versionOfScala212

    private def apply(m: String, v: String) = "io.gatling" % s"gatling-$m" % v
  }

  object akka {
    val version = "2.5.9"

    val actor = akka("actor")
    val remote = akka("remote")
    val testkit = akka("testkit")

    private val experimentalSuffix = "-experimental"

    private def apply(m: String, v: String = version, experimental: Boolean = false) = "com.typesafe.akka" %% s"akka-$m${if (experimental) experimentalSuffix else ""}" % v
  }

  val mockito = "org.mockito" % "mockito-core" % "2.3.11"

  val jsr305 = "com.google.code.findbugs" % "jsr305" % "2.0.3" // To avoid compile failure caused by fatal warning
}
