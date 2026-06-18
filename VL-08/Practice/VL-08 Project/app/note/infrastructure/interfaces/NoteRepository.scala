package note.infrastructure.interfaces

import com.google.inject.ImplementedBy
import note.domain.models.Note
import note.infrastructure.SlickNoteRepository

import scala.concurrent.Future

@ImplementedBy(classOf[SlickNoteRepository])
trait NoteRepository {
  def findAll: Future[Seq[Note]]
  def findById(id: Long): Future[Option[Note]]
  def create(note: Note): Future[Note]
  def update(id: Long, note: Note): Future[Option[Note]]
  def delete(id: Long): Future[Boolean]
}
