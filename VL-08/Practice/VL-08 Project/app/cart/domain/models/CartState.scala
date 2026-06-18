package cart.domain.models

import play.api.libs.json.{Format, Json}

/** Antwort-DTO: der nach außen sichtbare Zustand des Warenkorbs. `status`
  * spiegelt den FSM-Zustand (empty / active / checkedOut).
  */
case class CartState(
    cartId: String,
    status: String,
    items: Seq[CartItem],
    total: Double
)

object CartState {
  given format: Format[CartState] = Json.format[CartState]

  def from(cartId: String, status: String, items: Iterable[CartItem]): CartState =
    CartState(
      cartId = cartId,
      status = status,
      items = items.toSeq,
      total = items.map(i => i.price * i.quantity).sum
    )
}
