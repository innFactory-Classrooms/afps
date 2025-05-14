package controllers

import cats.data.EitherT
import domain.models.Person
import org.apache.pekko.actor.ActorSystem
import play.api.libs.json.Json
import play.api.mvc.*

import javax.inject.*
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future, Promise}

@Singleton
class HelloWorldController @Inject() (
    cc: ControllerComponents,
    actorSystem: ActorSystem
)(using ExecutionContext)
    extends AbstractController(cc) {

  def message = Action.async {
    getFutureMessage(1.second).map(* => Ok("Hello"))
  }

  def messageDynamic(msg: String) = Action.async {
    val futureEither: Future[Either[String, String]] = EitherT {
      Future.successful(Right[String, String](msg))
    }.value
    val futureResult = futureEither.map(
      _.fold(
        error => BadRequest(error),
        value => Ok(value)
      )
    )
    futureResult
  }

  def getPerson: Action[AnyContent] = Action { request =>
    // Option[JsValue]
    val rawJson = request.body.asJson
    // Option[Person]
    rawJson
      .map(
        _.validate[Person].fold(
          errors => {
            val errorString = errors
              .map(elem => {
                val path  = elem._1.path.mkString(".")
                val error = elem._2.mkString(", ")
                s"${path}: ${error}"
              })
              .mkString(", ")
            BadRequest(errorString)
          },
          person => Ok(Json.toJson(person))
        )
      )
      .getOrElse(BadRequest("No content"))
  }

  private def getFutureMessage(delayTime: FiniteDuration): Future[Unit] = {
    val promise: Promise[Unit] = Promise[Unit]()
    actorSystem.scheduler.scheduleOnce(delayTime) {
      promise.success(())
    }(using actorSystem.dispatcher) // run scheduled tasks using the actor system's dispatcher
    promise.future
  }

}
