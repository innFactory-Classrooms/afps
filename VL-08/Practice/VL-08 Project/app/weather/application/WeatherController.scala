package weather.application

import javax.inject.*
import play.api.*
import play.api.libs.json.Json
import play.api.mvc.*
import weather.domain.interfaces.WeatherService
import weather.domain.models.City

import scala.concurrent.ExecutionContext

@Singleton
class WeatherController @Inject() (
    cc: ControllerComponents,
    weatherService: WeatherService
)(using ExecutionContext)
    extends AbstractController(cc) {

  def getWeatherForCity: Action[City] = Action.async(parse.json[City]) {
    (request: Request[City]) =>
      weatherService.getWeatherForCity(request.body).value.map {
        case Left(value)  => InternalServerError(s"Error: ${value}")
        case Right(value) => Ok(Json.toJson(value))
      }
  }

}
