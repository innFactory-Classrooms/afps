package repositories

import cats.data.EitherT
import com.google.inject.Inject
import db.Tables
import domain.models.*
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

class SlickTodoRepository @Inject() (db: Database)(using ExecutionContext) extends TodoRepository {
  import slick.jdbc.PostgresProfile.api.*

  private def todoBaseQuery = Tables.Todo.joinLeft(Tables.Category).on((todo, category) => todo.categoryId === category.id)

  override def create(dto: TodoCreateDto): EitherT[Future, TodoError, Todo] = {
    val insertStatement: DBIOAction[Tables.TodoRow, NoStream, Effect.Write] = (Tables.Todo returning Tables.Todo) += Tables.TodoRow(
      // Siehe Dokumentation von +=, hier werden Auto-Increment Spalten übersprungen. Wir haben in V001__init.sql die id
      // Spalte als SERIAL definiert was in einer Postgres DB einer auto-inc Spalte entspricht.
      id = -1,
      title = dto.title,
      description = dto.description,
      done = false
    )
    runAsEitherT(for {
      todoRow     <- insertStatement
      todoBaseRow <- todoBaseQuery.filter { case (todo, _) => todo.id === todoRow.id }.result.head
    } yield todoBaseRow).map(mapRowToTodo)
  }

  // Übung
  override def getById(id: Long): EitherT[Future, TodoError, Todo] = {
    val query = todoBaseQuery
      .filter { case (todo, _) => todo.id === id.toInt }
      .result
      .headOption
    runAsEitherT(query).flatMap {
      case Some(row) => EitherT.rightT(mapRowToTodo(row))
      case None      => EitherT.leftT(TodoNotFoundError())
    }
  }

  // Übung
  // - Controller um einen query parameter - "done" erweitern und bis hier durchreichen
  // - Nur Todos ausgeben welche dem Filter entsprechen (Erinnerung: TableQuery ist an Collections angelehnt)
  override def getAll(done: Option[Boolean]): EitherT[Future, TodoError, Seq[Todo]] = {
    val query = todoBaseQuery
      .filterOpt(done) { case ((todo, _), done) =>
        todo.done === done
      }
      .result
    runAsEitherT(query)
      // Map des Ergebnisses, hier noch Seq[Tables.TodoRow]
      .map(
        // Map auf Sequence Ebene jedes Tables.TodoRow zu einem Todo aus unserer Domain
        _.map(mapRowToTodo)
      )
  }

  // Übung
  override def update(id: Long, dto: TodoUpdateDto): EitherT[Future, TodoError, Todo] = {
    val action = for {
      existingRow <- Tables.Todo.filter(_.id === id.toInt).result.head
      _ <- Tables.Todo
        .filter(_.id === id.toInt)
        .update(
          existingRow.copy(
            title = dto.title.getOrElse(existingRow.title),
            description = dto.description.getOrElse(existingRow.description),
            done = dto.done.getOrElse(existingRow.done)
          )
        )
      updatedRow <- todoBaseQuery.filter { case (todo, _) => todo.id === id.toInt }.result.head
    } yield mapRowToTodo(updatedRow)
    runAsEitherT(action)
  }

  // Übung
  override def delete(id: Long): EitherT[Future, TodoError, Unit] =
    runAsEitherT(Tables.Todo.filter(_.id === id.toInt).delete).map(_ => ())

  private def runAsEitherT[R](action: DBIOAction[R, ?, ?]): EitherT[Future, TodoError, R] = EitherT(
    db.run(action)
      // Erfolg des Futures auf Right mappen
      .map(Right(_))
      // Wenn das Future fehlschlägt (Failure) auf einen Left mappen
      .recover { case ex =>
        Left(TodoUnexpectedError(s"Datenbankfehler: ${ex.getMessage}"))
      }
  )

  private def mapRowToTodo(tuple: (Tables.TodoRow, Option[Tables.CategoryRow])) = {
    val (todoRow, categoryRow) = tuple
    Todo(
      id = todoRow.id,
      title = todoRow.title,
      description = todoRow.description,
      done = todoRow.done,
      category = categoryRow.map(row =>
        Category(
          id = row.id,
          name = row.name
        )
      )
    )
  }
  // Tipp: Könnte mit "chimney" automatisiert werden: https://chimney.readthedocs.io/
}
