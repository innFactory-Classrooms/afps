package todo.domain

import common.db.models.HasId

case class Todo(id: Long = 0)

object Todo {
  given HasId[Todo] with {
    def id(t: Todo): Long = t.id
    def withId(t: Todo, id: Long): Todo = t.copy(id = id)
  }
}
