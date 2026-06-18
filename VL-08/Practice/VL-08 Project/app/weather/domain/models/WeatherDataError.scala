package weather.domain.models

import common.models.errors.ApplicationError

case class WeatherDataError(
    statusCode: Int,
    message: String
) extends ApplicationError
