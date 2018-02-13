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
    , libraryDependencies ++= {
      Seq(
        akka.actor
        , akka.remote
        , akka.testkit % Test
        , scalaTest % Test
        , mockito % Test
        , jsr305 % Test
      ) ++ {
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2L, scalaMajor)) if scalaMajor == 12 =>
            Seq(
              gatling.coreOfScala212
              , gatling.testFrameworkOfScala212
              , gatling.highchartsOfScala212
            )
          case Some((2L, scalaMajor)) if scalaMajor <= 11 =>
            Seq(
              gatling.coreOfScala211
              , gatling.testFrameworkOfScala211
              , gatling.highchartsOfScala211
            )
        }
      }
    }
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