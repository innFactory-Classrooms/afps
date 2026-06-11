package todo.infrastructure

import common.db.InMemoryDBImpl
import todo.domain.Todo

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TodoRepositoryImpl @Inject()()(using ExecutionContext) {
  private val inMemoryDB = new InMemoryDBImpl[Todo]
}
