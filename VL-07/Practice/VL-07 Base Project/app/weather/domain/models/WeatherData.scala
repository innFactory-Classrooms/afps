package weather.domain.models

import play.api.libs.json.{Format, Json}

case class WeatherData(
    name: String,
    latitude: Double,
    longitude: Double,
    elevation: Double,
    temperature: Double,
    temperatureUnit: String,
    windSpeed: Double,
    windSpeedUnit: String
) extends ExtendedToString

object WeatherData {
  given format: Format[WeatherData] = Json.format[WeatherData]
}