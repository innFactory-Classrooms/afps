package weather.infrastructure

import cats.data.EitherT
import com.google.inject.Inject
import com.typesafe.config.Config
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import weather.domain.models.{WeatherData, WeatherDataError}
import weather.infrastructure.dto.OpenMeteoWeatherResponse
import weather.infrastructure.interfaces.WeatherClient

import java.net.http.HttpClient
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

private[infrastructure] class OpenMeteoWeatherClient @Inject() (
    config: Config,
    wslient: WSClient
)(
    using ec: ExecutionContext
) extends WeatherClient
    with Logging {
  private val client = HttpClient.newHttpClient()

  private def processSuccessStatus(
      name: String,
      callId: String
  )(
      jsonString: String,
      wsResponse: WSResponse
  ) = {
    logger.debug(s"[$callId] raw response boyd: $jsonString")
    val weatherResponseData = wsResponse.json.as[OpenMeteoWeatherResponse]
    val weatherDataResult = WeatherData(
      name = name,
      latitude = weatherResponseData.latitude,
      longitude = weatherResponseData.longitude,
      elevation = weatherResponseData.elevation,
      temperature = weatherResponseData.current.temperature_2m,
      temperatureUnit = weatherResponseData.current_units.temperature_2m,
      windSpeed = weatherResponseData.current.wind_speed_10m,
      windSpeedUnit = weatherResponseData.current_units.wind_speed_10m
    )
    logger.debug(
      s"[${Thread.currentThread().getName}][$callId] received result: $weatherResponseData"
    )
    Right(weatherDataResult)
  }

  def currentWeather(
      name: String,
      latitude: Double,
      longitude: Double
  ): EitherT[Future, WeatherDataError, WeatherData] = EitherT {
    val startTime = Instant.now()
    val callId = startTime.toEpochMilli.toString.takeRight(6)
    val responseProcessing = processSuccessStatus(name, callId)
    val baseUrl = config.getString("openmeteo.base-url")

    logger.info(
      s"[${Thread.currentThread().getName}][$callId] starting currentWeather request for lat: $latitude lon: $longitude"
    )

    Thread.sleep(
      (Math.random() * 10000).toLong
    ) // Sleep for 1 second to avoid rate limiting of open-meteo

    val response = wslient
      .url(baseUrl)
      .withQueryStringParameters(
        "latitude" -> latitude.toString,
        "longitude" -> longitude.toString,
        "current" -> "temperature_2m,wind_speed_10m"
      )
      .get()

    response.map(wsResponse => {
      val endTime = Instant.now()
      val duration = endTime.toEpochMilli - startTime.toEpochMilli
      val jsonString = Json.prettyPrint(wsResponse.json)

      logger.info(
        s"[${Thread.currentThread().getName}][$callId] api call status code ${wsResponse.status} | duration: $duration milliseconds"
      )

      wsResponse.status match {
        case 200 => responseProcessing(jsonString, wsResponse)
        case nonSuccessStatusCode =>
          logger.warn(
            s"[${Thread.currentThread().getName}][$callId] failure when calling api: $jsonString"
          )
          Left(WeatherDataError(nonSuccessStatusCode, jsonString))
      }
    })
  }
}
