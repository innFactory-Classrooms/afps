package repositories

import cats.data.EitherT
import com.google.inject.Inject
import domain.models.*
import generated.db.MainTables
import generated.db.XPostgresProfile.api.*
import io.scalaland.chimney.Transformer
import io.scalaland.chimney.dsl.*
import slick.dbio.{DBIOAction, Effect, NoStream}
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

class SlickTodoRepository @Inject() (db: Database)(using ExecutionContext) extends TodoRepository {

  private def todoBaseQuery = MainTables.Todo.joinLeft(MainTables.Category).on((todo, category) => todo.categoryId === category.id)

  override def create(dto: TodoCreateDto): EitherT[Future, TodoError, Todo] = {
    // Siehe Dokumentation von +=, hier werden Auto-Increment Spalten übersprungen. Wir haben in V001__init.sql die id
    // Spalte als SERIAL definiert was in einer Postgres DB einer auto-inc Spalte entspricht.
    val insertStatement: DBIOAction[MainTables.TodoRow, NoStream, Effect.Write] = (MainTables.Todo returning MainTables.Todo) += dto
      .into[MainTables.TodoRow]
      .withFieldConst(_.id, -1)
      .withFieldConst(_.categoryId, None)
      .withFieldConst(_.done, false)
      .transform
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
      // Map des Ergebnisses, hier noch Seq[MainTables.TodoRow]
      .map(
        // Map auf Sequence Ebene jedes MainTables.TodoRow zu einem Todo aus unserer Domain
        _.map(mapRowToTodo)
      )
  }

  // Übung
  override def update(id: Long, dto: TodoUpdateDto): EitherT[Future, TodoError, Todo] = {
    val action = for {
      existingRow <- MainTables.Todo.filter(_.id === id.toInt).result.head
      _ <- MainTables.Todo
        .filter(_.id === id.toInt)
        .update(existingRow.using(dto).ignoreNoneInPatch.patch)
      updatedRow <- todoBaseQuery.filter { case (todo, _) => todo.id === id.toInt }.result.head
    } yield mapRowToTodo(updatedRow)
    runAsEitherT(action)
  }

  // Übung
  override def delete(id: Long): EitherT[Future, TodoError, Unit] =
    runAsEitherT(MainTables.Todo.filter(_.id === id.toInt).delete).map(_ => ())

  private def runAsEitherT[R](action: DBIOAction[R, ?, ?]): EitherT[Future, TodoError, R] = EitherT(
    db.run(action)
      // Erfolg des Futures auf Right mappen
      .map(Right(_))
      // Wenn das Future fehlschlägt (Failure) auf einen Left mappen
      .recover { case ex =>
        Left(TodoUnexpectedError(s"Datenbankfehler: ${ex.getMessage}"))
      }
  )

  private def mapRowToTodo(tuple: (MainTables.TodoRow, Option[MainTables.CategoryRow])) = {
    val (todoRow, categoryRow)   = tuple
    given Transformer[Int, Long] = _.toLong
    todoRow
      .into[Todo]
      .withFieldConst(_.category, categoryRow.transformInto[Option[Category]])
      .transform
  }
}
