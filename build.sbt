import ReleaseTransformations._

lazy val beanPuree = (project in file ("."))
  .settings(
    name := "beanpuree",
    scalaVersion := "2.12.7",
    crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.8", "2.13.0-RC2"),
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
    organization := "me.limansky",
    incOptions := incOptions.value.withLogRecompileOnMacro(false),
    libraryDependencies ++= Seq(
      "com.chuusai"         %% "shapeless"        % "2.3.3",
      "org.typelevel"       %% "macro-compat"     % "1.1.1",
      "org.scala-lang"      % "scala-reflect"     % scalaVersion.value    % Provided,
      "org.scala-lang"      % "scala-compiler"    % scalaVersion.value    % Provided,
      "org.scalatest"       %% "scalatest"        % "3.0.8-RC4"           % Test
    ) ++ {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 10)) => Seq(
          "org.scalamacros" %% "quasiquotes" % "2.1.0",
          compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.patch)
        )
        case Some((2, x)) if x == 11 || x == 12 => Seq(
          compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch)
        )
        case Some((2, 13)) => Seq()
        case _ => sys.error("Unsupported Scala version")
      }
    },
    publishSettings,
    releaseSettings
  )

lazy val publishSettings = Seq(
  licenses += ("Apache 2.0 License", url("http://www.apache.org/licenses/LICENSE-2.0")),
  homepage := Some(url("http://github.com/limansky/beanpuree")),
  publishMavenStyle := true,
  Test / publishArtifact := false,
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/limansky/beanpuree"),
      "scm:git:git@github.com/limansky/beanpuree.git"
    )
  ),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  developers := List(
    Developer("limansky", "Mike Limansky", "mike.limansky@gmail.com", url("http://github.com/limansky"))
  )
)

lazy val releaseSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    pushChanges
  )
)
