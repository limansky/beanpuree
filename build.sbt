import ReleaseTransformations._

val scalaTestVersion = "3.2.19"

lazy val beanPuree = (project in file ("."))
  .settings(
    name := "beanpuree",
    scalaVersion := "2.13.16",
    crossScalaVersions := Seq("2.11.12", "2.12.20", "2.13.16"),
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
    organization := "me.limansky",
    incOptions := incOptions.value.withLogRecompileOnMacro(false),
    libraryDependencies += {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 11)) => "com.chuusai" %% "shapeless" % "2.3.11"
        case _             => "com.chuusai" %% "shapeless" % "2.3.13"
      }
    },
    libraryDependencies ++= Seq(
      "org.scala-lang"      %  "scala-reflect"              % scalaVersion.value    % Provided,
      "org.scala-lang"      %  "scala-compiler"             % scalaVersion.value    % Provided,
      "org.scalatest"       %% "scalatest-core"             % scalaTestVersion      % Test,
      "org.scalatest"       %% "scalatest-shouldmatchers"   % scalaTestVersion      % Test,
      "org.scalatest"       %% "scalatest-flatspec"         % scalaTestVersion      % Test
    ) ++ {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, x)) if x == 11 || x == 12 => Seq(
          compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch)
        )
        case Some((2, 13)) => Seq.empty
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

ThisBuild / credentials ++= (for {
  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq
