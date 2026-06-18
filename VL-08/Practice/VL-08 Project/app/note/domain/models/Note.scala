package note.domain.models

import play.api.libs.json.{Format, Json}

case class Note(
    id: Long = 0L,
    title: String,
    done: Boolean = false
)

object Note {
  given format: Format[Note] = Json.format[Note]
}
