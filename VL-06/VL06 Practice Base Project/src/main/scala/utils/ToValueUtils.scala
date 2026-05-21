package utils

import cats.data.{EitherT, OptionT}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object ToValueUtils {
  def toValue(
      either: EitherT[Future, ?, ?] | OptionT[Future, ?]
  ): Future[?] = either match {
    case e: EitherT[Future, ?, ?] => e.value
    case o: OptionT[Future, ?]    => o.value
  }
}
