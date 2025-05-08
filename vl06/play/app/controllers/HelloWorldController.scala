package controllers

import javax.inject._

import org.apache.pekko.actor.ActorSystem
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}

@Singleton
class HelloWorldController @Inject() (cc: ControllerComponents, actorSystem: ActorSystem)(using ExecutionContext) extends AbstractController(cc) {

  def message = Action.async {
    getFutureMessage(1.second).map(* => Ok("Hello"))
  }

  private def getFutureMessage(delayTime: FiniteDuration): Future[Unit] = {
    val promise: Promise[Unit] = Promise[Unit]()
    actorSystem.scheduler.scheduleOnce(delayTime) {
      promise.success(())
    }(using actorSystem.dispatcher) // run scheduled tasks using the actor system's dispatcher
    promise.future
  }

}
