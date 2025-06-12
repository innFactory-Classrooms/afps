import org.flywaydb.core.Flyway
import slick.jdbc.{MySQLProfile, PostgresProfile}
import slick.jdbc.meta.MTable

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.collection.mutable

object DatabaseCodegen {
  given ExecutionContext = ExecutionContext.global

  def main(args: Array[String]): Unit =
    args.toList match {
      case pkg :: schemas :: migrationBaseLocation :: outputDir :: Nil =>
        run(pkg, schemas.split(",").toSeq, migrationBaseLocation, outputDir)
      case _ =>
        println("""
            |Usage:
            |  DatabaseCodegen <pkg> <schemas> <migrationBaseLocation> <outputDir>
            |  pkg: The package name for the generated code
            |  schemas: Comma separated list of schemas to generate code for these must match folders in the <migrationBaseLocation>
            |  migrationBaseLocation: The base location of the migration files
            |  outputDir: The output directory for the generated code
                      """.stripMargin.trim)
        System.exit(1)
    }

  private def run(
      pkg: String,
      schemas: Seq[String],
      migrationBaseLocation: String,
      outputDir: String
  ): Unit = {
    val schemasByDbType = schemas.groupBy { schemaDef =>
      schemaDef.split(":").head
    }

    if (schemasByDbType.contains("postgresql")) {
      println("Starting postgresql database")
      val (containerId, pgPort) = DockerUtils.runPgContainer("15")
      val jdbcUrl = s"jdbc:postgresql://localhost:$pgPort/test"

      try {
        processSchemas(
          schemasByDbType("postgresql"),
          jdbcUrl,
          "org.postgresql.Driver",
          "test",
          "test",
          pkg,
          migrationBaseLocation,
          outputDir
        )
      } catch case e: Throwable => println(s"$e")
      finally {
        DockerUtils.removeContainer(containerId)
      }
    }

    if (schemasByDbType.contains("mariadb")) {
      println("Starting mariadb database")
      val (containerId, mariaPort) = DockerUtils.runMariaDbContainer("10.5")
      val jdbcUrl = s"jdbc:mysql://localhost:$mariaPort/test"

      try {
        processSchemas(
          schemasByDbType("mariadb"),
          jdbcUrl,
          "com.mysql.cj.jdbc.Driver",
          "root",
          "test",
          pkg,
          migrationBaseLocation,
          outputDir
        )
      } catch case e: Throwable => println(s"$e")
      finally {
        DockerUtils.removeContainer(containerId)
      }
    }
  }

  private def processSchemas(
      schemas: Seq[String],
      jdbcUrl: String,
      driver: String,
      username: String,
      password: String,
      pkg: String,
      migrationBaseLocation: String,
      outputDir: String
  ): Unit = {
    var isPostgresql = false

    val allEnumResults = mutable.ArrayBuffer.empty[EnumGenerator.Result]
    for (schemaDef <- schemas) {
      val (dbType, schema, modifier) = schemaDef match {
        case s"$db:$schema:$modifier" => (db, schema, Some(modifier))
        case s"$db:$schema"           => (db, schema, None)
      }

      println(s"Processing $dbType, $schema, $modifier")

      if (dbType == "postgresql") {
        isPostgresql = true
      }

      val schemas = if (schema == "legacy") {
        Seq("products", "notifications", "processes", "contacts", "documents", "iam")
      } else {
        Seq(schema)
      }

      val flyway = Flyway.configure
        .driver(driver)
        .dataSource(jdbcUrl, username, password)
        .baselineOnMigrate(true)
        .group(true)
        .schemas(schemas*)
        .locations(s"filesystem:$migrationBaseLocation/$schema")
        .load()

      val migrateResult = flyway.migrate()
      println(s"Migrated $schema to version ${migrateResult.targetSchemaVersion}")

      println(s"Generating enums for $schema")

      val enumResult = if (dbType == "postgresql") {
        val enumObjName = s"${schema.capitalize}Enums"
        val enumResult = EnumGenerator.generatePg(jdbcUrl, username, password, schema, pkg, enumObjName, outputDir)
        allEnumResults += enumResult
        println(s"Enums generated (${enumResult.enums.size})")
        Some(enumResult)
      } else {
        None
      }

      generateTables(jdbcUrl, schema, pkg, outputDir, modifier, enumResult, dbType)
    }

    if (isPostgresql) {
      ProfileGenerator.generate(pkg, outputDir, allEnumResults.toSeq)
    }
  }

  private def generateTables(
      jdbcUrl: String,
      schema: String,
      pkg: String,
      outputDir: String,
      modifier: Option[String],
      enumResult: Option[EnumGenerator.Result],
      dbType: String
  ): Unit = {
    println(s"Generating slick code for $schema")

    val db = dbType match {
      case "postgresql" => PostgresProfile.api.Database.forURL(jdbcUrl, "test", "test")
      case "mariadb"    => MySQLProfile.api.Database.forURL(jdbcUrl, "root", "test")
    }

    val excludedTables = Seq("flyway_schema_history", "doctrine_migration_versions")

    val profile = dbType match {
      case "postgresql" => PostgresProfile
      case "mariadb"    => MySQLProfile
    }

    val modelAction = profile.createModel(
      Some(
        MTable
          .getTables(None, Some(schema), Some("%"), Some(Seq("VIEW", "MATERIALIZED VIEW", "TABLE")))
          .map(_.filterNot(t => excludedTables.contains(t.name.name)))
      )
    )

    val model = Await.result(db.run(modelAction), Duration(20, TimeUnit.SECONDS))

    val codeGenerator = modifier match {
      case Some("dynamic") =>
        new CustomSchemaSourceCodeGenerator(
          model = model,
          isDynamic = true,
          schemaPrefix = schema == "legacy",
          enumResult = enumResult
        )
      case _ =>
        new CustomSchemaSourceCodeGenerator(
          model = model,
          isDynamic = false,
          schemaPrefix = schema == "legacy",
          enumResult = enumResult
        )
    }

    val className = s"${schema.capitalize}Tables"
    codeGenerator.writeToFile(
      dbType match {
        case "postgresql" => s"$pkg.XPostgresProfile"
        case "mariadb"    => "slick.jdbc.MySQLProfile"
      },
      outputDir,
      pkg,
      className,
      s"$className.scala"
    )

    val tablesFile = File(s"$outputDir/${pkg.replace(".", "/")}/$className.scala")
    tablesFile.setReadable(true, false)
    tablesFile.setWritable(true, false)
    tablesFile.setExecutable(false, false)

    println(s"Slick code generated for $schema ($outputDir/${pkg.replace(".", "/")}/$className.scala)")
  }

}
