package services

import cats.data.OptionT
import interfaces.UserService
import models.*
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class UserServiceImpl extends UserService {

  private var fakeUserDB = Map(
    1 -> User(1, "Alice", "alice@mail.com", 35),
    2 -> User(2, "Bob", "bob@mail.com", 22),
    3 -> User(3, "Charlie", "charlie@mail.com", 41),
    5 -> User(5, "Eve", "eve@mail.com", 29),
    84 -> User(84, "Mallory", "mallory@mail.com", 20)
  )

  def findUserById(
      userId: Int
  ): OptionT[Future, User] = OptionT(
    Future(
      fakeUserDB.get(userId)
    )
  )

  def findUserByMail(
      email: String
  ): OptionT[Future, User] = OptionT(
    Future(
      fakeUserDB.values.find(_.email == email)
    )
  )

  def insertUser(
      user: User
  ): OptionT[Future, User] = OptionT(
    Future {
      if (fakeUserDB.contains(user.id)) throw new Exception("User with this ID already exists")
      else if (fakeUserDB.exists((_, user) => user.email == user.email)) throw new Exception("User with this email already exists")
      else if (user.age < 18) throw new Exception("User must be 18 or older")
      else {
        fakeUserDB += (user.id -> user)
        Some(user)
      }
    }
  )

}
