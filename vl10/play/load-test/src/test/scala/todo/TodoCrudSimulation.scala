package todo

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class TodoCrudSimulation extends Simulation {

  private val baseUrl = "http://localhost:9000"
  private val httpConf = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  private val feeder = Iterator.continually(
    Map(
      "title"       -> s"todo-${java.util.UUID.randomUUID}",
      "description" -> s"desc-${System.nanoTime}"
    )
  )

  private val create =
    exec(
      http("create-todo")
        .post("/todos")
        .body(
          StringBody(
            """{ "title": "${title}", "description": "${description}" }"""
          )
        )
        .asJson
        .check(status.is(201))
        .check(jsonPath("$.id").saveAs("todoId"))
    )

  // Übung:
  // Tipp um auf todoId zuzugreifen in einem get:
  // .get(session => s"/todos/${session("todoId").as[Long]}")
  // private val read =
  // private val update =
  // private val delete =

  private val crudScenario =
    scenario("CRUD")
      .feed(feeder)
      .exec(
        create
        // Übung: read,update,delete
      )

  setUp(
    crudScenario.inject(
      constantUsersPerSec(50).during(120.seconds)
      // Übung:
      // - Weitere User hinzufügen z.B. über den Verlauf von 60 Sekunden aufbauen von 0 auf 100 User
    )
  ).protocols(httpConf)
    .assertions(
      // Übung:
      // - >=99% erfolgreiche Requests
      // - ~Antwortzeit <200ms
    )
}
