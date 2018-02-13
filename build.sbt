import Dependencies._
import Settings._
import _root_.io.gatling.sbt.GatlingPlugin

lazy val root = (project in file(".")).
  settings(coreSettings ++ noPublishSettings).
  settings(name := "gatling-akka-root").
  aggregate(gatling_akka)

lazy val gatling_akka = (project in file("gatling-akka")).
  enablePlugins(GatlingPlugin).
  settings(coreSettings).
  settings(
    name := "gatling-akka"
    , libraryDependencies ++= Seq(
      akka.actor
      , akka.remote
      , akka.testkit % Test
      , gatling.core % Provided
      , gatling.testFramework % Test
      , gatling.highcharts % Test
      , scalaTest % Test
      , mockito % Test
      , jsr305 % Test
    )
    , executeTests in Test := Def.task {
      val testResults = (executeTests in Test).value
      val gatlingResults = (executeTests in Gatling).value
      val overall = (testResults.overall, gatlingResults.overall) match {
        case (TestResult.Passed, TestResult.Passed) => TestResult.Passed
        case (TestResult.Error, _) => TestResult.Error
        case (_, TestResult.Error) => TestResult.Error
        case (TestResult.Failed, _) => TestResult.Failed
        case (_, TestResult.Failed) => TestResult.Failed
      }
      Tests.Output(overall,
        testResults.events ++ gatlingResults.events,
        testResults.summaries ++ gatlingResults.summaries)
    }.value
  )