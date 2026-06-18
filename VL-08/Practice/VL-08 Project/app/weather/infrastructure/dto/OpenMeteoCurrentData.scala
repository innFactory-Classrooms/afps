package weather.infrastructure.dto

import play.api.libs.json.{Format, Json}

case class OpenMeteoCurrentData(
 temperature_2m: Double,
 wind_speed_10m: Double
)

object OpenMeteoCurrentData {
  given format: Format[OpenMeteoCurrentData] = Json.format[OpenMeteoCurrentData]
}