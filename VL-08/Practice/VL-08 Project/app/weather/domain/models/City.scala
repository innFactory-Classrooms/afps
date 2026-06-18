package weather.domain.models

import play.api.libs.json.{Format, Json}

case class City(
    name: String,
    lat: Double,
    lon: Double
) extends ExtendedToString

object City {
  given format: Format[City] = Json.format[City]
}