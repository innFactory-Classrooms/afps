package interfaces

import cats.data.OptionT
import models.*

import scala.concurrent.Future

trait UserService {
  def findUserById(
      userId: Int
  ): OptionT[Future, User]

  def findUserByMail(
      email: String
  ): OptionT[Future, User]

  def insertUser(
     user: User
  ): OptionT[Future, User]
}

object UserService {
  val instance = new services.UserServiceImpl()
  def getInstance(): UserService = instance
}
