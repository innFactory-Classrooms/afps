package note.domain

import cats.data.EitherT
import com.google.inject.Inject
import common.models.errors.ApplicationError
import note.domain.interfaces.NoteService
import note.domain.models.{Note, NoteNotFound}
import note.infrastructure.interfaces.NoteRepository

import scala.concurrent.{ExecutionContext, Future}

class NoteServiceImpl @Inject() (repository: NoteRepository)(
    using ExecutionContext
) extends NoteService {

  def findAll: Future[Seq[Note]] = repository.findAll

  def findById(id: Long): EitherT[Future, ApplicationError, Note] =
    EitherT.fromOptionF[Future, ApplicationError, Note](
      repository.findById(id),
      NoteNotFound(s"Note $id nicht gefunden")
    )

  def create(note: Note): Future[Note] = repository.create(note)

  def delete(id: Long): EitherT[Future, ApplicationError, Unit] =
    EitherT {
      repository.delete(id).map {
        case true  => Right(())
        case false => Left(NoteNotFound(s"Note $id nicht gefunden"))
      }
    }
}
