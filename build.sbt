import Dependencies._
import Settings._
import io.gatling.sbt.GatlingPlugin

lazy val root = (project in file(".")).
  settings(coreSettings).
  settings(name := "gatling-akka-root").
  aggregate(gatling_akka)

lazy val gatling_akka = (project in file("gatling-akka")).
  enablePlugins(GatlingPlugin).
  settings(coreSettings).
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
    ),
    executeTests in Test <<= (executeTests in Test, executeTests in Gatling) map {
      case (testResults, gatlingResults)  =>
        val overall =
          if (testResults.overall.id < gatlingResults.overall.id)
            gatlingResults.overall
          else
            testResults.overall
        Tests.Output(overall,
          testResults.events ++ gatlingResults.events,
          testResults.summaries ++ gatlingResults.summaries)
    }
  )