package domain.models

sealed trait TodoError

case class TodoNotFoundError() extends TodoError

case class TodoValidationError(
    message: String
) extends TodoError
