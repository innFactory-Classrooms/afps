package note.application

import javax.inject.*
import play.api.libs.json.Json
import play.api.mvc.*
import note.domain.interfaces.NoteService
import note.domain.models.Note

import scala.concurrent.ExecutionContext

@Singleton
class NoteController @Inject() (
    cc: ControllerComponents,
    noteService: NoteService
)(using ExecutionContext)
    extends AbstractController(cc) {

  def getAll: Action[AnyContent] = Action.async {
    noteService.findAll.map(notes => Ok(Json.toJson(notes)))
  }

  def getById(id: Long): Action[AnyContent] = Action.async {
    noteService.findById(id).value.map {
      case Right(note) => Ok(Json.toJson(note))
      case Left(error) => NotFound(error.message)
    }
  }

  def create: Action[Note] = Action.async(parse.json[Note]) {
    (request: Request[Note]) =>
      noteService.create(request.body).map(note => Created(Json.toJson(note)))
  }

  def delete(id: Long): Action[AnyContent] = Action.async {
    noteService.delete(id).value.map {
      case Right(_)    => NoContent
      case Left(error) => NotFound(error.message)
    }
  }
}
