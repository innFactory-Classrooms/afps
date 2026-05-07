import interfaces.WeatherService
import models.City
import services.OpenMeteoWeatherService

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.DurationInt

object MainApplication {

  private val cities: List[City] = List(
    City("Rosenheim", -47.8564, 12.1221),
    City("München", 48.137154, 11.576124),
    City("Berlin", 52.520008, 13.404954),
    City("Hamburg", 53.551086, 9.993682),
    City("Paris", 48.8566, 2.3522),
    City("Invalid Coordinates", -999.8566, 200.3522),
    City("London", 51.5072, -0.1276),
    City("New York", 40.7128, -74.0060),
    City("Tokyo", 35.6762, 139.6503),
    City("Sydney", -33.8688, 151.2093),
    City("Cape Town", -33.9249, 18.4241)
  )

  @main def main(): Unit = {
    given ec: ExecutionContext = global
    val maxTimeout = 30.seconds
    Await.result(asyncApplication, maxTimeout)
  }

  def asyncApplication(using ec: ExecutionContext): Future[Any] = {
    given ExecutionContext = ec
    val service: WeatherService = new OpenMeteoWeatherService()


    ???
  }


}
