package domain.models

case class Pet(name: String)
object Pet {
  import play.api.libs.json.*
  given Format[Pet] = Json.format[Pet]
}

case class Person(
    firstName: String,
    lastName: Option[String],
    age: Option[Int],
    pet: Option[Pet]
)

object Person {
  import play.api.libs.json.*
  given Format[Person] = Json.format[Person]
}
