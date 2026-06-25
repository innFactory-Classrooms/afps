package todo.application

import org.apache.pekko.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}
import org.apache.pekko.stream.{ClosedShape, Materializer, OverflowStrategy}
import GraphDSL.Implicits.*
import org.apache.pekko.NotUsed

import javax.inject.*
import play.api.*
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.*
import todo.domain.Todo

import java.time.Instant
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

@Singleton
class TodoController @Inject() (
    cc: ControllerComponents
)(using ExecutionContext, Materializer)
    extends AbstractController(cc) {

  def getTodos(): Action[AnyContent] = Action {
    Ok.chunked(
      Source(
        LazyList.range(1, 5000000).map(id => Todo(id, Instant.now().toString))
      ).map { todo =>
        Json.toJson(todo)
      }
    )
  }

  def getTodosNonChunked(): Action[AnyContent] = Action {
    Ok(
      Json.toJson(
        List.range(1, 5000000).map(id => Todo(id, Instant.now().toString))
      )
    )
  }

  def todosGraph(): Action[AnyContent] = Action {
    val list = LazyList.range(1, 10)
    val (publisher, sink) = Sink.asPublisher[JsValue](false).preMaterialize()
    val source: Source[JsValue, NotUsed] = Source.fromPublisher(publisher)

    val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>

      val bcast = b.add(Broadcast[Int](2)) // 1 Eingang  -> 2 Ausgänge
      val merge = b.add(Merge[Int](2)) // 2 Eingänge -> 1 Ausgang

      val f1 = Flow[Int].buffer(5, OverflowStrategy.backpressure).throttle(1, 2.seconds).log("flow1")
      val f2 = Flow[Int].map(_ * 100).log("flow2")
      val f3 = Flow[Int].map(v => Todo(v, Instant.now().toString))
      val f4 = Flow[Todo].map(Json.toJson)

      Source(list) ~> bcast
      bcast.out(0) ~> f1 ~> merge.in(0)
      bcast.out(1) ~> f2 ~> merge.in(1)
      merge ~> f3 ~> f4 ~> sink
      ClosedShape
    })

    graph.run()

    Ok.chunked(source)
  }




}
