ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.3"

lazy val root = (project in file("."))
  .settings(
    name := "VL05 Practice Base Project",
    libraryDependencies += "org.scala-lang" %% "toolkit" % "0.7.0"
  )
