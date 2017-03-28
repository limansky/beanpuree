lazy val beanPuree = (project in file ("."))
  .settings(
    name := "beanpuree",
    version := "0.1",
    scalaVersion := "2.12.1",
    crossScalaVersions := Seq("2.11.8", "2.12.1"),
    organization := "me.limansky",
    incOptions := incOptions.value.withLogRecompileOnMacro(false),
    libraryDependencies ++= Seq(
      "com.chuusai"         %% "shapeless"        % "2.3.2",
      "org.typelevel"       %% "macro-compat"     % "1.1.1",
      "org.scala-lang"      % "scala-reflect"     % scalaVersion.value    % Provided,
      "org.scala-lang"      % "scala-compiler"    % scalaVersion.value    % Provided,
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.patch),
      "org.scalatest"       %% "scalatest"        % "3.0.1"         % Test
    )
  )
