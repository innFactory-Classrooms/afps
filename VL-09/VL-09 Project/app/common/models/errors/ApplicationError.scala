package common.models.errors

trait ApplicationError extends Throwable {
  def message: String
}
