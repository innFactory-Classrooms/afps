ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := "3.3.5"

val pekkoVersion = "1.1.3"

lazy val root = (project in file("."))
  .settings(
    name := "pekko-basics",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion,
      "org.apache.pekko" %% "pekko-actor-testkit-typed" % pekkoVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    ),
  )

