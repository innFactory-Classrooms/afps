package weather.infrastructure.interfaces

import com.google.inject.ImplementedBy
import cats.data.EitherT
import weather.domain.models.*
import weather.infrastructure.OpenMeteoWeatherClient

import scala.concurrent.Future

@ImplementedBy(classOf[OpenMeteoWeatherClient])
private[weather] trait WeatherClient {

  def currentWeather(
      name: String,
      latitude: Double,
      longitude: Double
  ): EitherT[Future, WeatherDataError, WeatherData]

}
