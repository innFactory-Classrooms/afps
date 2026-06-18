package cart.domain.models

import play.api.libs.json.{Format, Json}

case class CartItem(
    productId: String,
    name: String,
    quantity: Int,
    price: Double
)

object CartItem {
  given format: Format[CartItem] = Json.format[CartItem]
}
