package common.db.models

trait HasId[T] {
  def id(t: T): Long
  def withId(t: T, id: Long): T
}