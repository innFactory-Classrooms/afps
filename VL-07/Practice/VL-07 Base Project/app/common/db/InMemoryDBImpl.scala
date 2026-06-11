package common.db

import common.db.models.HasId

import java.util.concurrent.atomic.AtomicLong
import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}

class InMemoryDBImpl[T](using h: HasId[T], ec: ExecutionContext){

  // thread-sicher: TrieMap + AtomicLong (kein manuelles synchronized nötig)
  private val store = TrieMap.empty[Long, T]
  private val idGen = new AtomicLong(0L)

  def findAll: Future[Seq[T]] = {
    Future.successful(store.values.toSeq.sortBy(h.id))
  }

  def findById(id: Long): Future[Option[T]] =
    Future.successful(store.get(id))

  def create(data: T): Future[T] = {
    val id = idGen.incrementAndGet()
    val saved: T = h.withId(data, id)
    store.put(id, saved)
    Future.successful(saved)
  }

  def update(id: Long, data: T): Future[Option[T]] =
    Future.successful {
      if (store.contains(id)) {
        val updated = h.withId(data, id)
        store.put(id, updated)
        Some(updated)
      } else None
    }

  def delete(id: Long): Future[Boolean] =
    Future.successful(store.remove(id).isDefined)
}
