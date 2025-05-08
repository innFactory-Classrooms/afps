package repositories

import cats.data.EitherT
import com.google.inject.{ImplementedBy, Inject}
import domain.models.{Todo, TodoCreateDto, TodoError, TodoNotFoundError, TodoUpdateDto}
import play.api.mvc.Result
import play.api.mvc.Results.NotFound

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[InMemoryTodoRepository])
trait TodoRepository {
  def create(dto: TodoCreateDto): EitherT[Future, TodoError, Todo]
  def getById(id: Long): EitherT[Future, TodoError, Todo]
  def getAll(): EitherT[Future, TodoError, Seq[Todo]]
  def update(id: Long, dto: TodoUpdateDto): EitherT[Future, TodoError, Todo]
  def delete(id: Long): EitherT[Future, TodoError, Unit]
}

class InMemoryTodoRepository @Inject() ()(using ExecutionContext) extends TodoRepository {
  private val todos: mutable.Map[Long, Todo] = mutable.Map.empty
  private var currentId: Long                = 0

  override def create(dto: TodoCreateDto): EitherT[Future, TodoError, Todo] = EitherT {
    Future {
      currentId += 1
      val todo = Todo(currentId, dto.title, dto.description, false)
      todos += (currentId -> todo)
      Right(todo)
    }
  }

  override def getById(id: Long): EitherT[Future, TodoError, Todo] = EitherT {
    Future {
      todos.get(id).toRight(TodoNotFoundError())
    }
  }

  override def getAll(): EitherT[Future, TodoError, Seq[Todo]] = EitherT {
    Future {
      Right(todos.values.toSeq)
    }
  }

  override def update(id: Long, dto: TodoUpdateDto): EitherT[Future, TodoError, Todo] = EitherT {
    Future {
      todos.get(id) match {
        case Some(existingTodo) =>
          val updatedTodo = existingTodo.copy(
            title = dto.title.getOrElse(existingTodo.title),
            description = dto.description.getOrElse(existingTodo.description),
            done = dto.done.getOrElse(existingTodo.done)
          )
          todos.update(id, updatedTodo)
          Right(updatedTodo)
        case None => Left(TodoNotFoundError())
      }
    }
  }

  override def delete(id: Long): EitherT[Future, TodoError, Unit] = EitherT {
    Future {
      if (todos.contains(id)) {
        todos -= id
        Right(())
      } else {
        Left(TodoNotFoundError())
      }
    }
  }
}
