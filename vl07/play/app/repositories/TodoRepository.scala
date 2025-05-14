package repositories

import cats.data.EitherT
import com.google.inject.{ImplementedBy, Inject}
import db.Tables
import domain.models.*
import slick.dbio.{DBIOAction, Effect, NoStream}
import slick.jdbc.JdbcBackend.Database

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[SlickTodoRepository])
trait TodoRepository {
  def create(dto: TodoCreateDto): EitherT[Future, TodoError, Todo]
  def getById(id: Long): EitherT[Future, TodoError, Todo]
  def getAll(): EitherT[Future, TodoError, Seq[Todo]]
  def update(id: Long, dto: TodoUpdateDto): EitherT[Future, TodoError, Todo]
  def delete(id: Long): EitherT[Future, TodoError, Unit]
}
