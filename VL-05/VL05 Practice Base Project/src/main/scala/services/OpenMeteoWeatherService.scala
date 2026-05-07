package services

import interfaces.WeatherService
import models.{WeatherData, WeatherDataError}

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.FutureConverters.*

class OpenMeteoWeatherService()(using ec: ExecutionContext)
    extends WeatherService {
  private val client = HttpClient.newHttpClient()

  def currentWeatherToEither(
      name: String,
      latitude: Double,
      longitude: Double
  ): Future[Either[WeatherDataError, WeatherData]] =
    currentWeather(name, latitude, longitude).map(Right(_)).recover {
      case v: WeatherDataError => Left(v)
    }

  def currentWeather(
      name: String,
      latitude: Double,
      longitude: Double
  ): Future[WeatherData] = {

    Thread.sleep(
      1000
    ) // Sleep for 1 second to avoid rate limiting of open-meteo

    val startTime = Instant.now()
    val callId = startTime.toEpochMilli.toString.takeRight(6)

    val url = s"https://api.open-meteo.com/v1/forecast" +
      s"?latitude=$latitude&longitude=$longitude&current=temperature_2m,wind_speed_10m"

    println(
      s"[OpenMeteoWeatherService][$callId] starting currentWeather request for lat: $latitude lon: $longitude"
    )
    println(s"[OpenMeteoWeatherService][$callId] calling api at: $url")

    val request = HttpRequest
      .newBuilder()
      .uri(URI.create(url))
      .GET()
      .build()

    client
      .sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .asScala
      .map(result => {

        val endTime = Instant.now()
        val duration = endTime.toEpochMilli - startTime.toEpochMilli

        println(
          s"[OpenMeteoWeatherService][$callId] api call status code ${result
            .statusCode()} | duration: $duration milliseconds"
        )

        if (result.statusCode() != 200) {
          println(
            s"[OpenMeteoWeatherService][$callId] failure when calling api: ${result.body}"
          )
          throw WeatherDataError(result.statusCode(), result.body())
        }

        val json = ujson.read(result.body())
        val weatherDataResult = WeatherData(
          name = name,
          latitude = json.obj("latitude").num,
          longitude = json.obj("longitude").num,
          elevation = json.obj("elevation").num,
          temperature = json.obj("current").obj("temperature_2m").num,
          temperatureUnit = json.obj("current_units").obj("temperature_2m").str,
          windSpeed = json.obj("current").obj("wind_speed_10m").num,
          windSpeedUnit = json.obj("current_units").obj("wind_speed_10m").str
        )

        println(
          s"[OpenMeteoWeatherService][$callId] received result: $weatherDataResult"
        )

        weatherDataResult
      })

  }
}
