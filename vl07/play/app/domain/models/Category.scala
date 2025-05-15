package domain.models

case class Category(
    id: Long,
    name: String
)

object Category {
  import play.api.libs.json.*
  given OFormat[Category] = Json.format[Category]
}
