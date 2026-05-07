package models

case class WeatherDataError(
    statusCode: Int,
    message: String
) extends Throwable
