package weather.domain

import cats.data.EitherT
import com.google.inject.Inject
import weather.domain.interfaces.WeatherService
import weather.domain.models.{City, WeatherData, WeatherDataError}
import weather.infrastructure.interfaces.WeatherClient

import scala.concurrent.{ExecutionContext, Future}

class WeatherServiceImpl @Inject() (weatherClient: WeatherClient)(
    using ExecutionContext
) extends WeatherService {

  def getWeatherForCity(
      city: City
  ): EitherT[Future, WeatherDataError, WeatherData] = {
    for {
      weatherResult <- weatherClient.currentWeather(
        city.name,
        city.lat,
        city.lon
      )
    } yield weatherResult
  }

}
