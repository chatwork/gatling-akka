import sbt._

object Dependencies {

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1"

  object gatling {
    val version = "2.2.3"

    val core = gatling("core")
    val testFramework = gatling("test-framework")
    val highcharts = "io.gatling.highcharts" % "gatling-charts-highcharts" % version

    private def apply(m: String, v: String = version) = "io.gatling" % s"gatling-$m" % v
  }

  object akka {
    val version = "2.4.17"

    val actor = akka("actor")
    val remote = akka("remote")
    val testkit = akka("testkit")

    private val experimentalSuffix = "-experimental"

    private def apply(m: String, v: String = version, experimental: Boolean = false) = "com.typesafe.akka" %% s"akka-$m${if (experimental) experimentalSuffix else ""}" % v
  }

  val mockito = "org.mockito" % "mockito-core" % "2.3.11"

  val jsr305 = "com.google.code.findbugs" % "jsr305" % "2.0.3" // To avoid compile failure caused by fatal warning
}
