package domain.models

case class Todo(
    id: Long,
    title: String,
    description: String,
    done: Boolean
)

object Todo {
  import play.api.libs.json.*
  given OFormat[Todo] = Json.format[Todo]
}
