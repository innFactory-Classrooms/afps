package domain.models

import cats.data.EitherT
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

import scala.concurrent.{ExecutionContext, Future}

case class TodoUpdateDto(
    title: Option[String],
    description: Option[String],
    done: Option[Boolean]
) {
  def validate(using ExecutionContext): EitherT[Future, TodoError, TodoUpdateDto] = EitherT {
    Future {
      (for {
        _ <- title.map(_.refineEither[MinLength[3]]).getOrElse(Right(()))
        _ <- description.map(_.refineEither[MinLength[3]]).getOrElse(Right(()))
      } yield this).left.map(error => TodoValidationError(error))
    }
  }
}

object TodoUpdateDto {
  import play.api.libs.json.*
  given OFormat[TodoUpdateDto] = Json.format[TodoUpdateDto]
}
