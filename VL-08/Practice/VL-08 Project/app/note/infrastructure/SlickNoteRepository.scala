package note.infrastructure

import slick.jdbc.H2Profile.api.*
import common.db.SlickInMemoryDatabase
import note.domain.models.Note
import note.infrastructure.NoteTable.notes
import note.infrastructure.interfaces.NoteRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/** Spricht die In-Memory-Datenbank ueber Slick an.
  *
  * Ablauf jeder Methode: Query/Action beschreiben -> db.run fuehrt sie aus
  * und liefert ein Future. Das Schema wird zentral in SlickInMemoryDatabase
  * angelegt.
  */
@Singleton
class SlickNoteRepository @Inject() (database: SlickInMemoryDatabase)(
    using ExecutionContext
) extends NoteRepository {

  private val db = database.db

  def findAll: Future[Seq[Note]] =
    db.run(notes.sortBy(_.id).result)

  def findById(id: Long): Future[Option[Note]] =
    db.run(notes.filter(_.id === id).result.headOption)

  def create(note: Note): Future[Note] =
    db.run(
      (notes returning notes.map(_.id) into ((n, id) => n.copy(id = id))) += note
    )

  def update(id: Long, note: Note): Future[Option[Note]] =
    db.run(notes.filter(_.id === id).update(note.copy(id = id))).map {
      case 0 => None
      case _ => Some(note.copy(id = id))
    }

  def delete(id: Long): Future[Boolean] =
    db.run(notes.filter(_.id === id).delete).map(_ > 0)
}
