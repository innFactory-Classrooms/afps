import sbt.Keys.javaOptions


ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.0"


lazy val root = (project in file("."))
  .settings(
    name := "spark",
    libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % "3.5.5", // Spark 3.5.5 is not yet released. Using 3.5.0
    "org.apache.spark" %% "spark-sql" % "3.5.5"   // Spark 3.5.5 is not yet released. Using 3.5.0
  )
  )