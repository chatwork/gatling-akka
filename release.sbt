import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease._
import Settings._

def updateReadmeFile(version: String, readme: String): Unit = {
  val readmeFile = file(readme)
  val newReadme = Predef.augmentString(IO.read(readmeFile)).lines.map { line =>
    val matchReleaseOrSnapshot = line.contains("SNAPSHOT") == version.contains("SNAPSHOT")
    if (line.startsWith("libraryDependencies") && matchReleaseOrSnapshot) {
      val regex = """\d{1,2}\.\d{1,2}\.\d{1,2}""".r
      regex.replaceFirstIn(line, version)
    } else line
  }.mkString("", "\n", "\n")
  IO.write(readmeFile, newReadme)
}


val updateReadme: (State) => State = { state: State =>
  val extracted = Project.extract(state)
  val git = new Git(extracted get baseDirectory)
  val scalaV = extracted get scalaBinaryVersion
  val v = extracted get version
  val org = extracted get organization
  val n = extracted get name
  val readmeFiles = Seq(
    "README.md"
  )
  readmeFiles.foreach(readme => updateReadmeFile(v, readme))
  readmeFiles.foreach { readme =>
    git.add(readme) ! state.log.toScalaProcessLogger
    git.commit("update " + readme, true) ! state.log.toScalaProcessLogger
  }
  git.cmd("diff", "HEAD^") ! state.log.toScalaProcessLogger
  state
}

commands += Command.command("updateReadme")(updateReadme)

val updateReadmeProcess = ReleaseStep(updateReadme)

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  updateReadmeProcess,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges
)