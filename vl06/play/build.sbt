lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name         := """play-framework""",
    version      := "1.0-SNAPSHOT",
    scalaVersion := "3.7.0",
    libraryDependencies ++= Seq(
      guice,
      "org.typelevel"      %% "cats-core" % "2.13.0",
      "io.github.iltotore" %% "iron"      % "3.0.1"
    )
  )
