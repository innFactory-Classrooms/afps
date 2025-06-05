package domain.models

import cats.data.EitherT
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

import scala.concurrent.{ExecutionContext, Future}

case class TodoCreateDto(
    title: String,
    description: String
) {
  def validate(using ExecutionContext): EitherT[Future, TodoError, TodoCreateDto] = EitherT {
    Future {
      (for {
        _ <- title.refineEither[MinLength[3]]
        _ <- description.refineEither[MinLength[3]]
      } yield this).left.map(error => TodoValidationError(error))
    }
  }
}

object TodoCreateDto {
  import play.api.libs.json.*
  given OFormat[TodoCreateDto] = Json.format[TodoCreateDto]
}
