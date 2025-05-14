val flywayVersion = "11.8.2"
val slickVersion  = "3.6.0"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name         := """play-framework""",
    version      := "1.0-SNAPSHOT",
    scalaVersion := "3.7.0",
    // Notwendig da sonst ein Versionskonflikt besteht
    dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.14.3",
    Compile / sourceGenerators += slick.taskValue,
    libraryDependencies ++= Seq(
      guice,
      "org.typelevel"      %% "cats-core" % "2.13.0",
      "io.github.iltotore" %% "iron"      % "3.0.1",

      // Postgres JDBC (Java Database Connectivity) Driver
      "org.postgresql" % "postgresql" % "42.7.5",

      // Flyway
      "org.flywaydb" % "flyway-core"                % flywayVersion,
      "org.flywaydb" % "flyway-database-postgresql" % flywayVersion,

      // Slick (Functional Relational Mapping)
      "com.typesafe.slick" %% "slick"          % slickVersion,
      "com.typesafe.slick" %% "slick-codegen"  % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
    )
  )

// Basiert auf https://github.com/slick/slick-codegen-example/blob/main/build.sbt
lazy val slick = taskKey[Seq[File]]("Generate Tables.scala")
slick := {
  val dir         = (Compile / sourceManaged).value
  val outputDir   = dir / "slick"
  val url         = "jdbc:postgresql://localhost:5432/test?user=test&password=test"
  val jdbcDriver  = "org.postgresql.Driver"
  val slickDriver = "slick.jdbc.PostgresProfile"
  val pkg         = "db"
  val cp          = (Compile / dependencyClasspath).value
  val s           = streams.value
  runner.value
    .run(
      "slick.codegen.SourceCodeGenerator",
      cp.files,
      Array(slickDriver, jdbcDriver, url, outputDir.getPath, pkg),
      s.log
    )
    .failed foreach (sys error _.getMessage)
  val file = outputDir / pkg / "Tables.scala"
  Seq(file)
}
