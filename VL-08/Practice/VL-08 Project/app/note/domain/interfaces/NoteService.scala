package note.domain.interfaces

import cats.data.EitherT
import com.google.inject.ImplementedBy
import common.models.errors.ApplicationError
import note.domain.NoteServiceImpl
import note.domain.models.Note

import scala.concurrent.Future

@ImplementedBy(classOf[NoteServiceImpl])
trait NoteService {
  def findAll: Future[Seq[Note]]
  def findById(id: Long): EitherT[Future, ApplicationError, Note]
  def create(note: Note): Future[Note]
  def delete(id: Long): EitherT[Future, ApplicationError, Unit]
}
