package repositories

import cats.data.EitherT
import com.google.inject.ImplementedBy
import domain.models.*

import scala.concurrent.Future

@ImplementedBy(classOf[SlickTodoRepository])
trait TodoRepository {
  def create(dto: TodoCreateDto): EitherT[Future, TodoError, Todo]
  def getById(id: Long): EitherT[Future, TodoError, Todo]
  def getAll(done: Option[Boolean]): EitherT[Future, TodoError, Seq[Todo]]
  def update(id: Long, dto: TodoUpdateDto): EitherT[Future, TodoError, Todo]
  def delete(id: Long): EitherT[Future, TodoError, Unit]
}
