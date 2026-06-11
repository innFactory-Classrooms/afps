package weather.infrastructure.dto

import play.api.libs.json.{Format, Json}

case class OpenMeteoWeatherResponse(
    latitude: Double,
    longitude: Double,
    elevation: Double,
    current: OpenMeteoCurrentData,
    current_units: OpenMeteoCurrentUnits
)

object OpenMeteoWeatherResponse {
  given format: Format[OpenMeteoWeatherResponse] = Json.format[OpenMeteoWeatherResponse]
}
