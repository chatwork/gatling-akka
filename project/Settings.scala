import sbt.Keys._
import sbt._
import org.scalastyle.sbt.ScalastylePlugin.autoImport._
import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._
import xerial.sbt.Sonatype.autoImport._

object Settings {

  implicit class SbtLoggerOps(val self: sbt.Logger) extends AnyVal {
    def toScalaProcessLogger: scala.sys.process.ProcessLogger = new scala.sys.process.ProcessLogger {
      private val _log = new FullLogger(self)

      override def out(s: => String): Unit = _log.info(s)

      override def err(s: => String): Unit = _log.error(s)

      override def buffer[T](f: => T): T = _log.buffer(f)
    }
  }

  val compileScalaStyle = taskKey[Unit]("compileScalaStyle")

  lazy val scalaStyleSettings = Seq(
    (scalastyleConfig in Compile) := file("scalastyle-config.xml")
    , compileScalaStyle := scalastyle.in(Compile).toTask("").value
    , (compile in Compile) := (compile in Compile).dependsOn(compileScalaStyle).value
  )

  lazy val scalaFmtSettings = Seq(
    scalafmtOnCompile in Compile := true
    , scalafmtTestOnCompile in Compile := true
  )

  lazy val noPublishSettings = Seq(
    skip in publish := true
  )

  val mavenSettings = Seq(
    sonatypeProfileName := "com.chatwork",
    publishTo := sonatypePublishTo.value,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := {
      _ => false
    },
    pomExtra := {
      <url>https://github.com/chatwork/gatling-akka</url>
        <licenses>
          <license>
            <name>The MIT License</name>
            <url>http://opensource.org/licenses/MIT</url>
          </license>
        </licenses>
        <scm>
          <url>https://github.com/chatwork/gatling-akka</url>
          <connection>scm:git:github.com/chatwork/gatling-akka</connection>
          <developerConnection>scm:git@github.com:chatwork/gatling-akka.git</developerConnection>
        </scm>
        <developers>
          <developer>
            <id>everpeace</id>
            <name>Shingo Omura</name>
          </developer>
          <developer>
            <id>TanUkkii007</id>
            <name>Yusuke Yasuda</name>
          </developer>
        </developers>
    },
    credentials := {
      val ivyCredentials = (baseDirectory in LocalRootProject).value / ".credentials"
      val result = Credentials(ivyCredentials) :: Nil
      result
    }
  )

  val coreSettings = Seq(
    organization := "com.chatwork",
    scalaVersion := "2.12.4",
    crossScalaVersions := Seq("2.11.8", "2.12.4"),
    scalacOptions ++= {
      Seq(
        "-feature",
        "-deprecation",
        "-unchecked",
        "-encoding",
        "UTF-8",
        "-Xfatal-warnings",
        "-language:existentials",
        "-language:implicitConversions",
        "-language:postfixOps",
        "-language:higherKinds",
        "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
        "-Ywarn-dead-code", // Warn when dead code is identified.
        "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
        "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
        "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'
        "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
        "-Ywarn-numeric-widen", // Warn when numerics are widened.
        // "-Ywarn-unused", // Warn when local and private vals, vars, defs, and types are are unused.
        "-Ywarn-unused-import" // Warn when imports are unused.
      ) ++ {
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2L, scalaMajor)) if scalaMajor == 12 =>
            Seq.empty
          case Some((2L, scalaMajor)) if scalaMajor <= 11 =>
            Seq(
              "-Yinline-warnings" // Emit inlining warnings. (Normally surpressed due to high volume)
            )
        }
      }
    }
    , autoAPIMappings := true
  ) ++ scalaStyleSettings ++ scalaFmtSettings ++ mavenSettings
}
