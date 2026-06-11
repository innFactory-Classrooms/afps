package weather.infrastructure.dto

import play.api.libs.json.{Format, Json}

case class OpenMeteoCurrentUnits(
                                  temperature_2m: String,
                                  wind_speed_10m: String
                                )
object OpenMeteoCurrentUnits {
  given format: Format[OpenMeteoCurrentUnits] = Json.format[OpenMeteoCurrentUnits]
}