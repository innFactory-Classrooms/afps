ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.3"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := "VL08 Base Project",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.13.0",
      "org.scala-lang" %% "toolkit" % "0.9.2",
      "com.typesafe.slick" %% "slick" % "3.6.1",
      "com.h2database" % "h2" % "2.4.240",
      "org.apache.pekko" %% "pekko-actor-typed" % play.core.PlayVersion.pekkoVersion,
      guice,
      ws,
      "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test
    ),
    scalacOptions ++= Seq(
      "-feature"
    )
  )
