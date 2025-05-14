package repositories

import cats.data.EitherT
import com.google.inject.Inject
import db.Tables
import domain.models.*
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

class SlickTodoRepository @Inject() (db: Database)(using ExecutionContext) extends TodoRepository {
  import slick.jdbc.PostgresProfile.api.*

  override def create(dto: TodoCreateDto): EitherT[Future, TodoError, Todo] = {
    val insertStatement: DBIOAction[Tables.TodoRow, NoStream, Effect.Write] = (Tables.Todo returning Tables.Todo) += Tables.TodoRow(
      // Siehe Dokumentation von +=, hier werden Auto-Increment Spalten übersprungen. Wir haben in V001__init.sql die id
      // Spalte als SERIAL definiert was in einer Postgres DB einer auto-inc Spalte entspricht.
      id = -1,
      title = dto.title,
      description = dto.description,
      done = false
    )
    runAsEitherT(insertStatement).map(mapRowToTodo)
  }

  // Übung
  override def getById(id: Long): EitherT[Future, TodoError, Todo] = ???

  // Übung (optional)
  // - Controller um einen query parameter - "done" erweitern und bis hier durchreichen
  // - Nur Todos ausgeben welche dem Filter entsprechen (Erinnerung: TableQuery ist an Collections angelehnt)
  override def getAll(): EitherT[Future, TodoError, Seq[Todo]] = {
    val query = Tables.Todo.result
    runAsEitherT(query)
      // Map des Ergebnisses, hier noch Seq[Tables.TodoRow]
      .map(
        // Map auf Sequence Ebene jedes Tables.TodoRow zu einem Todo aus unserer Domain
        _.map(mapRowToTodo)
      )
  }

  // Übung
  override def update(id: Long, dto: TodoUpdateDto): EitherT[Future, TodoError, Todo] = ???

  // Übung
  override def delete(id: Long): EitherT[Future, TodoError, Unit] = ???

  private def runAsEitherT[R](action: DBIOAction[R, ?, ?]): EitherT[Future, TodoError, R] = EitherT(
    db.run(action)
      // Erfolg des Futures auf Right mappen
      .map(Right(_))
      // Wenn das Future fehlschlägt (Failure) auf einen Left mappen
      .recover { case ex =>
        Left(TodoUnexpectedError(s"Datenbankfehler: ${ex.getMessage}"))
      }
  )

  private def mapRowToTodo(row: Tables.TodoRow) = Todo(
    id = row.id,
    title = row.title,
    description = row.description,
    done = row.done
  )
}
