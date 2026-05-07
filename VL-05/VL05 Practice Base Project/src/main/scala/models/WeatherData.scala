package models

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
