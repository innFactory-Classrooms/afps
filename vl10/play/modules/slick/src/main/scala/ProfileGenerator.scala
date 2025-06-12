import java.nio.file.{Files, Path, Paths}

object ProfileGenerator {
  def generate(
      pkg: String,
      targetDirectory: String,
      enumResults: Seq[EnumGenerator.Result]
  ): Unit = {
    println("Generating code for extended Postgres profile")
    val targetFile = Paths.get(targetDirectory, pkg.replaceAll("\\.", "/"), "XPostgresProfile.scala")
    writeToFile(targetFile, makeSourceFileContent(pkg, enumResults))
    println("Profile generation completed successfully!")
  }

  private def buildEnumSupport(
      enumResult: EnumGenerator.Result,
      `enum`: EnumGenerator.Enum
  ): String =
    val originalName = `enum`.name
    val name = s"${enumResult.schemaName.capitalize}${`enum`.label}"
    val importName = s"${enumResult.pkgName}.${enumResult.objName}.${`enum`.label}"

    s"""  // mapping for $originalName / $name
       |  implicit val ${name}TypeMapper: JdbcType[$importName] = createEnumJdbcType[$importName]("$originalName", _.value, $importName.fromString, false)
       |  implicit val ${name}ListTypeMapper: JdbcType[List[$importName]] = createEnumListJdbcType[$importName]("${NameConverter
        .enumNameToListName(
          originalName
        )}", _.value, $importName.fromString, false)
       |  implicit def ${name}ColumnExtensionMethodsBuilder(rep: Rep[${importName}]): EnumColumnExtensionMethods[${importName}, ${importName}] = createEnumColumnExtensionMethodsBuilder[${importName}](${name}TypeMapper, ${name}ListTypeMapper).apply(rep)
       |  implicit def ${name}OptionColumnExtensionMethodsBuilder(rep: Rep[Option[${importName}]]): EnumColumnExtensionMethods[${importName}, Option[${importName}]] = createEnumOptionColumnExtensionMethodsBuilder[${importName}](${name}TypeMapper, ${name}ListTypeMapper).apply(rep)""".stripMargin

  private def makeSourceFileContent(pkg: String, enumResults: Seq[EnumGenerator.Result]): String =
    s"""package $pkg

import com.github.tminglei.slickpg.*
import com.github.tminglei.slickpg.str.PgStringSupport
import com.github.tminglei.slickpg.trgm.PgTrgmSupport
import slick.jdbc.*
import play.api.libs.json.{JsValue, Json}
${enumResults.map(e => s"import ${e.pkgName}").distinct.mkString("\n")}

trait XPostgresProfile
    extends ExPostgresProfile
    with PgArraySupport
    with PgDate2Support
    with PgRangeSupport
    with PgHStoreSupport
    with PgSearchSupport
    with PgPlayJsonSupport
    with PgTrgmSupport
//    with PgPostGISSupport
    with PgNetSupport
    with PgLTreeSupport
    with PgEnumSupport
    with PgStringSupport {

  // jsonb support is in postgres 9.4.0 onward; for 9.3.x use "json"
  def pgjson: String = "jsonb"

  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
  override protected def computeCapabilities: Set[slick.basic.Capability] =
    super.computeCapabilities + slick.jdbc.JdbcCapabilities.insertOrUpdate

  override val api: CodegenAPI.type = CodegenAPI

  object CodegenAPI
      extends ExtPostgresAPI
      with ArrayImplicits
      with Date2DateTimeImplicitsDuration
      with JsonImplicits
      with NetImplicits
      with LTreeImplicits
      with RangeImplicits
      //      with PostGISImplicits
      with PgTrgmImplicits
      with HStoreImplicits
      with PlayJsonImplicits
      with SearchImplicits
      with SearchAssistants
      with PgStringImplicits {
    implicit val strListTypeMapper: DriverJdbcType[List[String]] =
      new SimpleArrayJdbcType[String]("text").to(_.toList)
    implicit val playJsonArrayTypeMapper: DriverJdbcType[List[JsValue]] =
      new AdvancedArrayJdbcType[JsValue](
        pgjson,
        s => utils.SimpleArrayUtils.fromString[JsValue](Json.parse)(s).orNull,
        v => utils.SimpleArrayUtils.mkString[JsValue](_.toString())(v)
      ).to(_.toList)

    ${indent(
        enumResults
          .flatMap(enumResult => enumResult.enums.map(`enum` => buildEnumSupport(enumResult, `enum`)))
          .mkString("\n\n")
      )}
  }
}

object XPostgresProfile extends XPostgresProfile
""".stripMargin

  private def indent(code: String): String = {
    val lines = code.split("\n")
    lines.tail.foldLeft(lines.head) { (out, line) =>
      out + '\n' +
        (if (line.isEmpty) line else "  " + line)
    }
  }

  private def writeToFile(f: Path, str: String): Unit = {
    Files.createDirectories(f.getParent)
    Files.write(f, str.getBytes)
  }
}
