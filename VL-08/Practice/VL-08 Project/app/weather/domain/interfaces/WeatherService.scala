package weather.domain.interfaces

import cats.data.EitherT
import com.google.inject.ImplementedBy
import weather.domain.models.{City, WeatherData, WeatherDataError}
import weather.domain.WeatherServiceImpl

import scala.concurrent.Future

@ImplementedBy(classOf[WeatherServiceImpl])
trait WeatherService {
  def getWeatherForCity(
      city: City
  ): EitherT[Future, WeatherDataError, WeatherData]
}
