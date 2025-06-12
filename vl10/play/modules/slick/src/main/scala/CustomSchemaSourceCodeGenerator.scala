import slick.codegen.SourceCodeGenerator
import slick.model.Model
import slick.sql.SqlProfile.ColumnOption
import scala.util.chaining.*

class CustomSchemaSourceCodeGenerator(
    model: Model,
    private val isDynamic: Boolean,
    private val schemaPrefix: Boolean,
    private val enumResult: Option[EnumGenerator.Result]
) extends SourceCodeGenerator(model) {
  // abuse ddl to generate sequence of all tables
  override def codeForDDL: String =
    s"\ndef allTables${if (isDynamic) "(using TenantContext)" else ""} = Seq(${tables.map(_.TableValue.name).filterNot(_.endsWith("View")).mkString(", ")})"

  // ensure to use our customized postgres driver at `import profile.simple.*`
  override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]): String =
    s"""
package ${pkg}
import play.api.libs.json.{Format, Json, Reads, Writes}

// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object $container extends $container {
  val profile: $profile = $profile
}

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait $container${parentType.map(t => s" extends $t").getOrElse("")} {
  val profile: $profile
  import profile.api.*
  import profile.api.given
  ${enumResult.map(res => s"import ${res.objName}.*").getOrElse("")}
  ${
        if schemaPrefix then """
  import generated.db.XPostgresProfile.GenericDateJdbcType
  import java.time.*
  import slick.jdbc.JdbcType
  
  implicit val date2DateTypeMapper: JdbcType[LocalDate] = new GenericDateJdbcType[LocalDate]("date", java.sql.Types.DATE)
  implicit val date2TimeTypeMapper: JdbcType[LocalTime] = new GenericDateJdbcType[LocalTime]("time", java.sql.Types.TIME)
  implicit val date2DateTimeTypeMapper: JdbcType[LocalDateTime] = new GenericDateJdbcType[LocalDateTime]("timestamp", java.sql.Types.TIMESTAMP)
  implicit val date2InstantTypeMapper: JdbcType[Instant] = new GenericDateJdbcType[Instant]("timestamp", java.sql.Types.TIMESTAMP)
""".stripMargin
        else ""
      }

  ${indent(code)}

}
          """.trim()

  override def Table =
    new Table(_) {
      table =>
      override def EntityType: AbstractSourceCodeEntityTypeDef = new EntityType {
        override def rawName: String = if (schemaPrefix)
          s"${table.model.name.schema.getOrElse("").capitalize}${entityName(table.model.name.table).capitalize}"
        else super.rawName

        override def code: String = {
          val args = columns
            .map(c =>
              c.default
                .map(v => s"${c.name}: ${c.exposedType} = $v")
                .getOrElse(
                  s"${c.name}: ${c.exposedType}"
                )
            )
            .mkString(", ")
          if (classEnabled) {
            val parentsString = (parents.take(1).map(" extends " + _) ++ parents.drop(1).map(" with " + _)).mkString("")
            (if (caseClassFinal) "final " else "") +
              s"""case class $name($args)$parentsString
                 |object $name {
                 |  given Format[$name] = Json.format[$name]
                 |}""".stripMargin
          } else {
            if (columns.size > 254)
              s"type $name = $types" // constructor method would exceed JVM parameter limit
            else s"""
type $name = $types
/** Constructor for $name providing default values if available in the database schema. */
def $name($args): $name = {
  ${compoundValue(columns.map(_.name))}
}
          """.trim
          }
        }
      }

      override def TableClass: AbstractSourceCodeTableClassDef = new TableClass {
        override def rawName: String = if (schemaPrefix)
          s"${table.model.name.schema.getOrElse("").capitalize}${tableName(table.model.name.table).capitalize}"
        else super.rawName

        override def star: String = if (schemaPrefix) {
          val struct = compoundValue(columns.map(c => if (c.asOption) s"Rep.Some(${c.name})" else s"${c.name}"))
          val rhs =
            if (mappingEnabled) s"($struct).mapTo[${typeName(rawName)}Row]"
            else
              struct
          s"def * = $rhs"
        } else super.star

        override def code: String = if isDynamic then
          val parentsString = parents.map(" with " + _).mkString("")
          val args          = Seq("\"" + table.model.name.table + "\"")
          s"""|
              | class $name(_tableTag: Tag, schemaName: String) extends profile.api.Table[$elementType](_tableTag, Some(schemaName), ${args
               .mkString(", ")})$parentsString {
              |  ${indent(body.map(_.mkString("\n")).mkString("\n\n"))}
              |  val foreignKeysAndPrimaryKey: Seq[(Rep[?], slick.lifted.TableQuery[?])] = $generateForeignKeysAndPrimaryKey
              | }
              |""".stripMargin
        else super.code
      }

      def generateForeignKeysAndPrimaryKey: String = {
        def toCamelCase(snake: String): String = {
          val parts = snake.split('_')
          parts.headOption.getOrElse("") + parts.drop(1).map(_.capitalize).mkString
        }

        def generateForeignKey(fk: slick.model.ForeignKey): String = {
          val columnNames = compoundValue(fk.referencingColumns.map(col => toCamelCase(col.name)))
          val referencedTableName =
            if (schemaPrefix)
              s"${fk.referencedTable.schema.getOrElse("").capitalize}${toCamelCase(fk.referencedTable.table).capitalize}"
            else toCamelCase(fk.referencedTable.table).capitalize
          s"($columnNames, $referencedTableName${if (isDynamic) "(this.schemaName)" else ""})"
        }

        def generatePrimaryKey(table: Table): Option[String] = {
          val pkColumns = table.model.columns.filter(_.name.equalsIgnoreCase("id"))
          if (pkColumns.nonEmpty) {
            val columnNames = compoundValue(pkColumns.map(pk => toCamelCase(pk.name)))
            val tableName =
              if (schemaPrefix)
                s"${table.model.name.schema.getOrElse("").capitalize}${toCamelCase(table.model.name.table).capitalize}"
              else toCamelCase(table.model.name.table).capitalize
            Some(s"($columnNames, $tableName${if (isDynamic) "(this.schemaName)" else ""})")
          } else {
            None
          }
        }

        def generateCompositePrimaryKey(pk: Option[slick.model.PrimaryKey]): Option[String] = pk.map { primaryKey =>
          val columnNames = compoundValue(primaryKey.columns.map(col => toCamelCase(col.name)))
          val referencedTableName =
            if (schemaPrefix)
              s"${table.model.name.schema.getOrElse("").capitalize}${toCamelCase(table.model.name.table).capitalize}"
            else toCamelCase(primaryKey.table.table).capitalize
          s"($columnNames, $referencedTableName${if (isDynamic) "(this.schemaName)" else ""})"
        }

        val foreignKeys = table.model.foreignKeys.map(generateForeignKey)

        val primaryKey          = generatePrimaryKey(table)
        val compositePrimaryKey = generateCompositePrimaryKey(table.model.primaryKey)

        val allKeys = (foreignKeys ++ primaryKey ++ compositePrimaryKey).mkString(", ")
        s"Seq($allKeys)"
      }

      override def factory: String = if (columns.size == 1 || isMappedToHugeClass) TableClass.elementType
      else s"(${TableClass.elementType}.apply).tupled"

      override def ForeignKey: (m: slick.model.ForeignKey) => AbstractSourceCodeForeignKeyDef =
        new ForeignKey(_) {
          override def code: String = {
            val pkTable = referencedTable.TableValue.name
            val (pkColumns, fkColumns) = referencedColumns
              .lazyZip(referencingColumns)
              .map { (p, f) =>
                val pk = s"r.${p.name}"
                val fk = f.name
                if (p.model.nullable && !f.model.nullable) (pk, s"Rep.Some($fk)")
                else if (!p.model.nullable && f.model.nullable) (s"Rep.Some($pk)", fk)
                else (pk, fk)
              }
              .unzip
            val fkExpr = compoundValue(fkColumns)
            val pkExpr = compoundValue(pkColumns)
            s"lazy val $name = " +
              s"""foreignKey("$dbName", $fkExpr, $pkTable${if (isDynamic) "(this.schemaName)" else ""})(r => $pkExpr, onUpdate=$onUpdate, onDelete=$onDelete)"""
          }
        }

      override def TableValue: AbstractSourceCodeTableValueDef = new TableValue {
        override def rawName: String = if (schemaPrefix)
          s"${table.model.name.schema.getOrElse("").capitalize}${tableName(table.model.name.table).capitalize}"
        else
          super.rawName

        override def code: String = if isDynamic then s"""|
                | def $name(schemaName: String) = new TableQuery(tag => new ${TableClass.name}(tag, schemaName))
                | def $name(using tc: TenantContext): TableQuery[$name] = $name(tc.tenant.id.dbSchemaName)
                |""".stripMargin
        else super.code
      }

      override def Column =
        new Column(_) {
          column =>
          // customize db type -> scala type mapping, pls adjust it according to your environment
          override def rawType: String = if schemaPrefix then super.rawType
          else
            column.model.options
              .find(_.isInstanceOf[ColumnOption.SqlType])
              .flatMap(tpe =>
                rawTypeMatcherBase(
                  table.model.name.table,
                  column.model.name,
                  tpe.asInstanceOf[ColumnOption.SqlType].typeName
                )
              )
              .getOrElse(sqlTypeMapper(column.model.tpe, super.rawType))
        }
    }

  private def rawTypeMatcherBase(tableName: String, columnName: String, typeName: String): Option[String] =
    typeName match {
      case "hstore"                                      => Option("Map[String, String]")
      case "_text" | "text[]" | "_varchar" | "varchar[]" => Option("List[String]")
      case "_int8" | "int8[]"                            => Option("List[Long]")
      case "_int4" | "int4[]"                            => Option("List[Int]")
      case "_int2" | "int2[]"                            => Option("List[Short]")
      case s: String                                     => rawTypeMatcherExtension(s).orElse(enumTypeMapper(tableName, columnName, s))
    }

  private def rawTypeMatcherExtension(typeName: String): Option[String] =
    typeName match {
      //          case "geometry" => Option("com.vividsolutions.jts.geom.Geometry")
      case "tsvector" => Option("com.github.tminglei.slickpg.TsVector")
      case _          => None
    }

  private def sqlTypeMapper(typeName: String, superRawType: String): String =
    typeName match {
      case "java.sql.Timestamp" => "java.time.Instant"
      case "java.sql.Date"      => "java.time.LocalDate"
      case _                    => superRawType
    }

  private def enumTypeMapper(tableName: String, columnName: String, typeName: String): Option[String] = {
    val pattern = s""""\\w+"."(.+)"""".r
    val maybeTypeName = typeName match {
      case pattern(name) => Some(name)
      case _             => None
    }
    enumResult.map(_.enums).getOrElse(Seq.empty).find(_.name == maybeTypeName.getOrElse(typeName)).map(_.label)
  }
}
