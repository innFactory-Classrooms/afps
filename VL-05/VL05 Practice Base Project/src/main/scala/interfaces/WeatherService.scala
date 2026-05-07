package interfaces

import models.*

import scala.concurrent.Future

trait WeatherService {
  def currentWeather(
      name: String,
      latitude: Double,
      longitude: Double
  ): Future[WeatherData]

  def currentWeatherToEither(
      name: String,
      latitude: Double,
      longitude: Double
  ): Future[Either[WeatherDataError, WeatherData]]
}
