package note.domain.models

import common.models.errors.ApplicationError

case class NoteNotFound(message: String) extends ApplicationError
