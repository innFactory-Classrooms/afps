ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.5"

lazy val root = (project in file("."))
  .settings(
    name := "proj-uebung",
    libraryDependencies ++= Seq(
      "io.github.iltotore" %% "iron" % "2.6.0",
      "org.scalactic" %% "scalactic" % "3.2.19",
      "org.scalatest" %% "scalatest" % "3.2.19" % "test"
    )
  )
