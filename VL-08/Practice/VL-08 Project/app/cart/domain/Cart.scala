package cart.domain

import org.apache.pekko.actor.typed.{ActorRef, Behavior, LogOptions}
import org.apache.pekko.actor.typed.scaladsl.{ActorContext, Behaviors}
import cart.domain.models.{CartItem, CartState}
import org.slf4j.event.Level

/** Shopping-Cart als endliche Zustandsmaschine (FSM).
  *
  * Jeder Zustand ist ein eigenes Behavior:
  *   empty  --AddItem-->  active  --Checkout-->  checkedOut
  *          <--RemoveItem (letzter Artikel)--
  *
  * Ungültige Übergänge (z.B. AddItem nach Checkout) werden ignoriert und der
  * unveränderte Zustand zurückgemeldet.
  */
object Cart {

  sealed trait Command
  final case class AddItem(item: CartItem, replyTo: ActorRef[CartState])
      extends Command
  final case class RemoveItem(productId: String, replyTo: ActorRef[CartState])
      extends Command
  final case class Get(replyTo: ActorRef[CartState]) extends Command
  final case class Checkout(replyTo: ActorRef[CartState]) extends Command

  def apply(cartId: String): Behavior[Command] = {
    Behaviors.setup { context =>
      context.log.info(s"[${cartId}] starting")
      Behaviors.logMessages(
        LogOptions().withLevel(Level.INFO), {
          empty(cartId) (using context)
        }
      )
    }

  }

  // Zustand 1: leerer Warenkorb
  private def empty(
      cartId: String
  )(using ctx: ActorContext[Command]): Behavior[Command] =
    Behaviors.receiveMessage {
      case AddItem(item, replyTo) =>
        val items = Map(item.productId -> item)
        replyTo ! CartState.from(cartId, "active", items.values)
        ctx.log.info(s"[${cartId}] current cart content $items")
        active(cartId, items) // Übergang -> active

      case Get(replyTo) =>
        replyTo ! CartState.from(cartId, "empty", Nil)
        ctx.log.info(s"[${cartId}] current cart is empty")
        Behaviors.same

      case RemoveItem(_, replyTo) =>
        replyTo ! CartState.from(cartId, "empty", Nil)
        ctx.log.info(s"[${cartId}] current cart is empty")
        Behaviors.same

      case Checkout(replyTo) =>
        // leerer Warenkorb kann nicht ausgecheckt werden -> Zustand bleibt
        replyTo ! CartState.from(cartId, "empty", Nil)
        ctx.log.info(s"[${cartId}] current cart is empty")
        Behaviors.same
    }

  // Zustand 2: aktiver Warenkorb mit mindestens einem Artikel
  private def active(
      cartId: String,
      items: Map[String, CartItem]
  )(using ctx: ActorContext[Command]): Behavior[Command] =
    Behaviors.receiveMessage {
      case AddItem(item, replyTo) =>
        val updated = items.updatedWith(item.productId) {
          case Some(existing) =>
            Some(existing.copy(quantity = existing.quantity + item.quantity))
          case None => Some(item)
        }
        replyTo ! CartState.from(cartId, "active", updated.values)
        ctx.log.info(s"[${cartId}] current cart content $updated")
        active(cartId, updated)

      case RemoveItem(productId, replyTo) =>
        val updated = items - productId
        ctx.log.info(s"[${cartId}] current cart content $updated")
        if (updated.isEmpty) {
          replyTo ! CartState.from(cartId, "empty", Nil)
          empty(cartId) // Übergang -> empty
        } else {
          replyTo ! CartState.from(cartId, "active", updated.values)
          active(cartId, updated)
        }

      case Get(replyTo) =>
        replyTo ! CartState.from(cartId, "active", items.values)
        Behaviors.same

      case Checkout(replyTo) =>
        replyTo ! CartState.from(cartId, "checkedOut", items.values)
        ctx.log.info(s"[${cartId}] current cart content $items")
        checkedOut(cartId, items) // Übergang -> checkedOut
    }

  // Zustand 3: abgeschlossen, keine Änderungen mehr möglich
  private def checkedOut(
      cartId: String,
      items: Map[String, CartItem]
  )(using ctx: ActorContext[Command]): Behavior[Command] =
    Behaviors.receiveMessage {
      case Get(replyTo) =>
        replyTo ! CartState.from(cartId, "checkedOut", items.values)
        Behaviors.same

      // alle modifizierenden Befehle sind ungültig -> Zustand unverändert
      case AddItem(_, replyTo) =>
        replyTo ! CartState.from(cartId, "checkedOut", items.values)
        Behaviors.same
      case RemoveItem(_, replyTo) =>
        replyTo ! CartState.from(cartId, "checkedOut", items.values)
        Behaviors.same
      case Checkout(replyTo) =>
        replyTo ! CartState.from(cartId, "checkedOut", items.values)
        Behaviors.same
    }
}
