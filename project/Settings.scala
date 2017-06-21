import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import org.scalastyle.sbt.ScalastylePlugin._
import sbt.Keys._
import sbt._
import xerial.sbt.Sonatype.autoImport.sonatypeProfileName

import scalariform.formatter.preferences._

object Settings {

  val compileScalaStyle = taskKey[Unit]("compileScalaStyle")

  lazy val scalaStyleSettings = Seq(
    (scalastyleConfig in Compile) := file("scalastyle-config.xml")
    , compileScalaStyle := scalastyle.in(Compile).toTask("").value
    , (compile in Compile) <<= (compile in Compile) dependsOn compileScalaStyle
  )

  val formatPreferences = FormattingPreferences()
    .setPreference(RewriteArrowSymbols, false)
    .setPreference(AlignParameters, true)
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(SpacesAroundMultiImports, true)
    .setPreference(DoubleIndentClassDeclaration, true)
    .setPreference(AlignArguments, true)


  lazy val noPublishSettings = Seq(
    publish := (),
    publishArtifact in Compile := false
  )

  val mavenSettings = Seq(
    sonatypeProfileName := "com.chatwork",
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
    scalaVersion := "2.11.8",
    scalacOptions ++= Seq(
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
      "-Yinline-warnings", // Emit inlining warnings. (Normally surpressed due to high volume)
      "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
      "-Ywarn-dead-code", // Warn when dead code is identified.
      "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
      "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
      "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'
      "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
      "-Ywarn-numeric-widen", // Warn when numerics are widened.
      "-Ywarn-unused", // Warn when local and private vals, vars, defs, and types are are unused.
      "-Ywarn-unused-import" // Warn when imports are unused.
    )
    , autoAPIMappings := true
  ) ++ scalaStyleSettings ++ SbtScalariform.scalariformSettings ++ Seq(
    ScalariformKeys.preferences in Compile := formatPreferences,
    ScalariformKeys.preferences in Test := formatPreferences
  ) ++ mavenSettings
}
