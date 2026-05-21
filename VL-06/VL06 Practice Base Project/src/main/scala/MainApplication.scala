import interfaces.UserService

import cats._
import cats.data._
import cats.syntax.all._

import models.User
import utils.ToValueUtils.toValue

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*

object MainApplication {

  val userService: UserService = UserService.getInstance()

  @main def main(): Unit = {
   Await.result(toValue(application()), 30.seconds)
  }

  def application(): EitherT[Future, ?, ?] | OptionT[Future, ?] = {
    userService.findUserById(1)
  }

}
