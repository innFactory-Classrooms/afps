import scala.collection.immutable.Seq

ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := "3.3.5"

val pekkoVersion = "1.1.3"
val pekkoJdbcVersion = "1.1.0"
val slickVersion = "3.5.1"

lazy val root = (project in file("."))
  .settings(
    name := "pekko-persistence",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion,
      "org.apache.pekko" %% "pekko-persistence-typed" % pekkoVersion,
      "org.apache.pekko" %% "pekko-persistence-jdbc" % pekkoJdbcVersion,
      "org.apache.pekko" %% "pekko-persistence-query" % pekkoVersion,
      "org.apache.pekko" %% "pekko-serialization-jackson" % pekkoVersion,
      "org.apache.pekko" %% "pekko-actor-testkit-typed" % pekkoVersion % Test,
      "org.apache.pekko" %% "pekko-persistence-testkit" % pekkoVersion % Test,
      "ch.qos.logback" % "logback-classic" % "1.4.14",
      "com.typesafe.slick" %% "slick" % slickVersion,
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "com.h2database" % "h2" % "2.2.224"
    )
  )