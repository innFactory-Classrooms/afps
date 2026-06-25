package todo.domain

import play.api.libs.json.{Format, Json}

case class Todo(id: Int, currentTime: String)

object Todo {
  given format: Format[Todo] = Json.format[Todo]
}