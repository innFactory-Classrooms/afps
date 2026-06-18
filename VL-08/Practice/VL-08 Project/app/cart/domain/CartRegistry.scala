package cart.domain

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.scaladsl.adapter.*

import java.util.concurrent.ConcurrentHashMap
import javax.inject.{Inject, Singleton}

/** Verwaltet pro `cartId` genau einen Cart-Aktor.
  *
  * `computeIfAbsent` ist atomar pro Schlüssel, daher wird der Aktor nur einmal
  * gespawnt (sonst gäbe es einen Namenskonflikt). Ohne Cluster-Sharding ist das
  * die einfachste Variante, einen Aktor pro Entität bereitzustellen.
  */
@Singleton
class CartRegistry @Inject() (system: ActorSystem) {

  private val carts =
    new ConcurrentHashMap[String, ActorRef[Cart.Command]]()

  def cartRef(cartId: String): ActorRef[Cart.Command] =
    carts.computeIfAbsent(
      cartId,
      id => system.spawn(Cart(id), s"cart-$id")
    )
}
