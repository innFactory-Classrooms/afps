package controllers

import cats.data.EitherT
import domain.models.*
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.*
import services.TodoService

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TodoController @Inject() (
    cc: ControllerComponents,
    todoService: TodoService
)(using ExecutionContext)
    extends AbstractController(cc) {
  def createTodo: Action[JsValue] = Action(parse.json).async { request =>
    request.body
      .validate[TodoCreateDto]
      .fold(
        errors => Future.successful(BadRequest(JsError.toJson(errors))),
        dto => toResult(todoService.createTodo(dto), (todo) => Created(Json.toJson(todo)))
      )
  }

  def getTodoById(id: Long): Action[AnyContent] = Action.async {
    toResult(todoService.getTodoById(id), (todo) => Ok(Json.toJson(todo)))
  }

  def getAllTodos: Action[AnyContent] = Action.async {
    toResult(todoService.getAllTodos(), (todos) => Ok(Json.toJson(todos)))
  }

  def updateTodo(id: Long): Action[JsValue] = Action(parse.json).async { request =>
    request.body
      .validate[TodoUpdateDto]
      .fold(
        errors => Future.successful(BadRequest(JsError.toJson(errors))),
        dto => toResult(todoService.updateTodo(id, dto), todo => Ok(Json.toJson(todo)))
      )
  }

  def deleteTodo(id: Long): Action[AnyContent] = Action.async {
    toResult(todoService.deleteTodo(id), * => NoContent)
  }

  private def toResult[T](eitherT: EitherT[Future, TodoError, T], r: T => Result) = eitherT.value.map {
    case Right(v)    => r(v)
    case Left(error) => mapTodoError(error)
  }

  private def mapTodoError(error: TodoError): Result = error match {
    case _: TodoNotFoundError       => NotFound("Todo not found")
    case TodoValidationError(error) => BadRequest(error)
    case TodoUnexpectedError(error) => InternalServerError(error)
  }
}
