package services

import cats.data.EitherT
import com.google.inject.{ImplementedBy, Inject, Singleton}
import domain.models.{Todo, TodoCreateDto, TodoError, TodoUpdateDto}
import repositories.TodoRepository

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[TodoServiceImpl])
trait TodoService {
  def createTodo(dto: TodoCreateDto): EitherT[Future, TodoError, Todo]
  def getTodoById(id: Long): EitherT[Future, TodoError, Todo]
  def getAllTodos(done: Option[Boolean]): EitherT[Future, TodoError, Seq[Todo]]
  def updateTodo(id: Long, dto: TodoUpdateDto): EitherT[Future, TodoError, Todo]
  def deleteTodo(id: Long): EitherT[Future, TodoError, Unit]
}

@Singleton
class TodoServiceImpl @Inject() (todoRepository: TodoRepository)(using ExecutionContext) extends TodoService {
  override def createTodo(dto: TodoCreateDto): EitherT[Future, TodoError, Todo] = for {
    _    <- dto.validate
    todo <- todoRepository.create(dto)
  } yield todo

  override def getTodoById(id: Long): EitherT[Future, TodoError, Todo] =
    todoRepository.getById(id)

  override def getAllTodos(done: Option[Boolean]): EitherT[Future, TodoError, Seq[Todo]] =
    todoRepository.getAll(done)

  override def updateTodo(id: Long, dto: TodoUpdateDto): EitherT[Future, TodoError, Todo] = for {
    _    <- dto.validate
    todo <- todoRepository.update(id, dto)
  } yield todo

  override def deleteTodo(id: Long): EitherT[Future, TodoError, Unit] =
    todoRepository.delete(id)
}
