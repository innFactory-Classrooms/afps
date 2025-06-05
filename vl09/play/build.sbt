import sbt.nio.file.FileTreeView
import sbt.{Def, File, Test}

val flywayVersion = "11.8.2"
val slickVersion  = "3.6.0"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, DockerPlugin)
  .settings(
    name         := """play-framework""",
    version      := "1.0-SNAPSHOT",
    scalaVersion := "3.7.0",
    // Notwendig da sonst ein Versionskonflikt besteht
    dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.14.3",
    Compile / sourceGenerators += dbGenCached.taskValue,
    dockerExposedPorts := Seq(9000),
    dockerBaseImage    := "eclipse-temurin:21.0.5_11-jre-noble",
    dockerEntrypoint   := Seq("/opt/docker/bin/play-framework", "-Dplay.server.pidfile.path=/dev/null"),
    libraryDependencies ++= Seq(
      guice,
      "org.typelevel"      %% "cats-core" % "2.13.0",
      "io.github.iltotore" %% "iron"      % "3.0.1",
      "io.scalaland"       %% "chimney"   % "1.8.1"
    ) ++ dbDependencies
  )

lazy val dbDependencies = {
  val SlickPgVersion             = "0.22.2"
  val SlickVersion               = "3.5.2"
  val testcontainersScalaVersion = "0.41.8"
  val flywayVersion              = "11.3.4"

  Seq(
    "org.flywaydb"         % "flyway-core"                     % flywayVersion,
    "org.flywaydb"         % "flyway-database-postgresql"      % flywayVersion,
    "org.postgresql"       % "postgresql"                      % "42.7.5",
    "com.dimafeng"        %% "testcontainers-scala-scalatest"  % testcontainersScalaVersion % Test,
    "com.dimafeng"        %% "testcontainers-scala-postgresql" % testcontainersScalaVersion % Test,
    "com.typesafe.slick"  %% "slick"                           % SlickVersion,
    "com.typesafe.slick"  %% "slick-hikaricp"                  % SlickVersion,
    "com.typesafe.slick"  %% "slick-codegen"                   % SlickVersion,
    "com.github.tminglei" %% "slick-pg"                        % SlickPgVersion,
    "com.github.tminglei" %% "slick-pg_play-json"              % SlickPgVersion
  )
}

lazy val slick = project
  .in(file("modules/slick"))
  .settings(
    scalaVersion := "3.7.0",
    libraryDependencies ++= dbDependencies,
    dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.17.3"
  )

def dbGenBase(schema: String, dbType: String): Def.Initialize[Task[() => Set[sbt.File]]] = Def.taskDyn {
  val confDir                = (Compile / resourceDirectory).value
  val migrationsDir          = confDir / "db" / "migration"
  val pkg                    = s"generated.db"
  val schemas                = Seq(s"$dbType:$schema")
  val sourceManagedDirectory = sourceManaged.value
  // Add "main" as root folder after "src_managed" to make the generated package automatically available
  val outputDirectory = sourceManagedDirectory / "main"
  val r               = (slick / Compile / runner).value
  val cp              = (slick / Compile / fullClasspath).value
  val s               = (slick / Compile / streams).value
  val args            = Seq[String](pkg, schemas.mkString(","), migrationsDir.toString, outputDirectory.toString)

  val outputDirectoryWithPackage = outputDirectory / pkg.replace(".", "/")

  def generate(): Set[File] =
    r.run("DatabaseCodegen", cp.files, args, s.log) match {
      case scala.util.Success(_) => (outputDirectoryWithPackage ** "*.scala").get.toSet
      case scala.util.Failure(e) => throw e
    }

  Def.task(() => generate())
}

lazy val dbGen = taskKey[Seq[File]]("Migrate all schemas and run slick codegen")
dbGen := dbGenBase("main", "postgresql").value().toSeq

lazy val dbGenCached = taskKey[Seq[File]]("Migrate all schemas and run slick codegen with caching")

val dbGenCachedTask = Def.task {
  val confDir        = (Compile / resourceDirectory).value
  val migrationsDir  = confDir / "db" / "migration"
  val migrationFiles = FileTreeView.default.list(migrationsDir.toGlob / ** / "*.sql").map(_._1.toFile).toSet
  val dbGenMain      = dbGenBase("main", "postgresql").value

  def generate(in: Set[File]): Set[File] = {
    println("Generating database code")
    dbGenMain()
  }
  FileFunction.cached(streams.value.cacheDirectory / "gen-db", FilesInfo.hash)(generate)(migrationFiles).toSeq
}

dbGenCached := dbGenCachedTask.value
