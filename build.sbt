import ReleaseTransformations._

val scalaTestVersion = "3.2.6"

lazy val beanPuree = (project in file ("."))
  .settings(
    name := "beanpuree",
    scalaVersion := "2.12.13",
    crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.13", "2.13.5"),
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
    organization := "me.limansky",
    incOptions := incOptions.value.withLogRecompileOnMacro(false),
    libraryDependencies ++= Seq(
      "com.chuusai"         %% "shapeless"                  % "2.3.3",
      "org.scala-lang"      %  "scala-reflect"              % scalaVersion.value    % Provided,
      "org.scala-lang"      %  "scala-compiler"             % scalaVersion.value    % Provided,
      "org.scalatest"       %% "scalatest-core"             % scalaTestVersion      % Test,
      "org.scalatest"       %% "scalatest-shouldmatchers"   % scalaTestVersion      % Test,
      "org.scalatest"       %% "scalatest-flatspec"         % scalaTestVersion      % Test
    ) ++ {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 10)) => Seq(
          "org.scalamacros" %% "quasiquotes"      % "2.1.1",
          "org.typelevel"   %% "macro-compat"     % "1.1.1",
          compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch)
        )
        case Some((2, x)) if x == 11 || x == 12 => Seq(
          compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch)
        )
        case Some((2, 13)) => Seq.empty
        case _ => sys.error("Unsupported Scala version")
      }
    },
    unmanagedSourceDirectories in Compile ++= {
      (unmanagedSourceDirectories in Compile).value.filter(_.getName == "scala").flatMap { dir =>
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, 10)) => Seq.empty
          case Some((2, x)) if x >= 11 => Seq(new File(dir.getPath + "-2.11+"))
          case _ => sys.error("Unsupported Scala version")
        }
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
  publishTo := sonatypePublishToBundle.value,
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
    releaseStepCommand("sonatypeBundleRelease"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)
