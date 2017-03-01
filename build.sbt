import Dependencies._
import Settings._
import io.gatling.sbt.GatlingPlugin

lazy val root = (project in file(".")).
  settings(coreSetting).
  settings(
    organization := "com.chatwork",
    name := "gatling-akka-root"
  ).aggregate(gatling_akka)

lazy val gatling_akka = (project in file("gatling-akka")).
  enablePlugins(GatlingPlugin).
  settings(coreSetting).
  settings(
    name := "gatling-akka",
    libraryDependencies ++= Seq(
      akka.actor,
      akka.remote,
      akka.testkit,
      gatling.core % Provided,
      gatling.testFramework % Test,
      gatling.highcharts % Test,
      scalaTest % Test
    )
  )