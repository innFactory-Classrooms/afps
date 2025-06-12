import java.nio.file.{Files, Path, Paths}
import java.sql.DriverManager
import scala.collection.mutable

/// Based on https://github.com/ratelware/pg-scala-enums
object EnumGenerator {
  case class Result(
      schemaName: String,
      enums: Seq[Enum],
      pkgName: String,
      objName: String,
      outputPath: Path
  )

  case class Enum(
      name: String,
      label: String
  )

  def generatePg(
      dbUrl: String,
      username: String,
      password: String,
      schemaName: String,
      pkg: String,
      enumObjName: String,
      outputDir: String
  ): Result = {
    val query =
      s"""
         |SELECT t.typname, e.enumlabel, e.enumsortorder
         |FROM pg_enum e
         |INNER JOIN pg_type t
         |ON t.oid=e.enumtypid
         |WHERE t.typnamespace = (SELECT oid FROM pg_namespace WHERE nspname = '$schemaName')
         |""".stripMargin
    Class.forName("org.postgresql.Driver")
    val connection = DriverManager.getConnection(dbUrl, username, password)

    try {
      val result = connection.createStatement().executeQuery(query)

      val enumNameToValueAndOrder = mutable.HashMap.empty[String, Seq[(String, Int)]]
      while (result.next()) {
        val name  = result.getString("typname")
        val label = result.getString("enumlabel")
        val order = result.getInt("enumsortorder")

        enumNameToValueAndOrder += (name -> enumNameToValueAndOrder.getOrElse(name, Seq()).+:((label, order)))
      }

      val enumsCode = enumNameToValueAndOrder.view.mapValues(_.sortBy(_._2).map(_._1)).toSeq.map { en =>
        val label = NameConverter.snakeToCamel(NameConverter.removeTypePostfix(en._1))
        (en._1, label, buildEnum(label, en._2), en._2)
      }

      val enums = enumsCode.map(en => Enum(en._1, en._2))

      val code =
        s"""package $pkg
           |
           |object $enumObjName {
           |  ${indent(enumsCode.map(_._3).mkString("\n\n"))}
           |}""".stripMargin
      val targetFile = Paths.get(outputDir, pkg.replaceAll("\\.", "/"), s"$enumObjName.scala")

      writeToFile(targetFile, code)

      Result(schemaName, enums, pkg, enumObjName, targetFile)
    } catch {
      case e: Exception =>
        println("Error during enum generation:")
        throw e
    } finally {
      connection.close()
    }
  }

  private def buildEnum(label: String, values: Seq[String]): String = {
    val cases        = values.map(v => s"case ${NameConverter.snakeToCamel(v)} extends $label(\"$v\")").mkString("\n  ")
    val stringToEnum = values.map(v => s""""$v" -> $label.${NameConverter.snakeToCamel(v)}""").mkString(",\n    ")

    s"""enum $label(val value: String) {
       |  $cases
       |}
       |
       |object $label {
       |  def fromString(value: String): $label = Map(
       |    $stringToEnum
       |  )(value)
       |}""".stripMargin
  }

  private def generateSlickMapping(enumName: String, values: Seq[String]): String = {
    val slickMappingName = s"${enumName}ColumnType"

    s"""implicit val $slickMappingName: BaseColumnType[$enumName] = MappedColumnType.base[$enumName, String](
       |  _.value,
       |  $enumName.fromString
       |)
       |""".stripMargin
  }

  private def writeToFile(f: Path, str: String): Path = {
    Files.createDirectories(f.getParent)
    Files.write(f, str.getBytes)
    f
  }

  private def indent(code: String): String = {
    val lines = code.split("\n")
    lines.tail.foldLeft(lines.head) { (out, line) =>
      out + '\n' +
        (if (line.isEmpty) line else "  " + line)
    }
  }
}
