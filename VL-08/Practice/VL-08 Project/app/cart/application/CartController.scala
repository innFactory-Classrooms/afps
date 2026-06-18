package cart.application

import javax.inject.*
import play.api.libs.json.Json
import play.api.mvc.*
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.actor.typed.Scheduler
import org.apache.pekko.actor.typed.scaladsl.adapter.*
import org.apache.pekko.actor.typed.scaladsl.AskPattern.*
import org.apache.pekko.util.Timeout
import cart.domain.{Cart, CartRegistry}
import cart.domain.models.{CartItem, CartState}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*

/** Spricht den Cart-FSM-Aktor per Request/Response (ask-Pattern) an.
  *
  * Jeder Endpunkt schickt eine Command-Nachricht an den Aktor und antwortet mit
  * dem zurückgemeldeten CartState.
  */
@Singleton
class CartController @Inject() (
    cc: ControllerComponents,
    registry: CartRegistry,
    system: ActorSystem
)(using ExecutionContext)
    extends AbstractController(cc) {

  // benötigt für das ask-Pattern
  private given Timeout = 3.seconds
  private given Scheduler = system.toTyped.scheduler

  def get(cartId: String): Action[AnyContent] = Action.async {
    registry
      .cartRef(cartId)
      .ask[CartState](Cart.Get(_))
      .map(state => Ok(Json.toJson(state)))
  }

  def addItem(cartId: String): Action[CartItem] =
    Action.async(parse.json[CartItem]) { (request: Request[CartItem]) =>
      registry
        .cartRef(cartId)
        .ask[CartState](Cart.AddItem(request.body, _))
        .map(state => Ok(Json.toJson(state)))
    }

  def removeItem(cartId: String, productId: String): Action[AnyContent] =
    Action.async {
      registry
        .cartRef(cartId)
        .ask[CartState](Cart.RemoveItem(productId, _))
        .map(state => Ok(Json.toJson(state)))
    }

  def checkout(cartId: String): Action[AnyContent] = Action.async {
    registry
      .cartRef(cartId)
      .ask[CartState](Cart.Checkout(_))
      .map(state => Ok(Json.toJson(state)))
  }
}
