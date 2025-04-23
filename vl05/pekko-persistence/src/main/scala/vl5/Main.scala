package vl5

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors

import scala.concurrent.duration.*
import scala.util.Random
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

/**
 *
 * Gesendet - Rosenheim: 13,1°C, Rio: 29,3°C, New York: 7,7°C
 * Temperatur in rosenheim: 13,1°C bei 2025-04-23T14:52:43.012789Z
 * Temperatur in rio: 29,3°C bei 2025-04-23T14:52:43.012797Z
 * Temperatur in new-york: 7,7°C bei 2025-04-23T14:52:43.012791Z
 * Gesendet - Rosenheim: -1,1°C, Rio: 26,2°C, New York: 20,1°C
 * Temperatur in rio: 26,2°C bei 2025-04-23T14:52:45.007930Z
 * Temperatur in rosenheim: -1,1°C bei 2025-04-23T14:52:45.007930Z
 * Temperatur in new-york: 20,1°C bei 2025-04-23T14:52:45.007930Z
 * Gesendet - Rosenheim: 3,2°C, Rio: 10,6°C, New York: 2,4°C
 * Temperatur in rosenheim: 3,2°C bei 2025-04-23T14:52:47.008278Z
 * Temperatur in new-york: 2,4°C bei 2025-04-23T14:52:47.009043Z
 * Temperatur in rio: 10,6°C bei 2025-04-23T14:52:47.008984Z
 *
 */


object Main {

  def initializeTables(): Unit = {
    import slick.jdbc.H2Profile.api._
    import scala.concurrent.Await
    import scala.concurrent.duration._
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.io.Source
    import scala.util.{Failure, Success, Try}

    println("Initialisiere Datenbanktabellen...")

    val db = Database.forURL(
      url = "jdbc:h2:mem:test-database;DB_CLOSE_DELAY=-1",
      user = "sa",
      password = "",
      driver = "org.h2.Driver"
    )

    // SQL-Datei einlesen
    val schemaSource = Source.fromResource("schema/h2.sql")
    val schemaSql = Try(schemaSource.mkString) match {
      case Success(sql) =>
        schemaSource.close()
        sql
      case Failure(ex) =>
        println(s"Fehler beim Lesen der Schema-Datei: ${ex.getMessage}")
        ""
    }

    if (schemaSql.nonEmpty) {
      // SQL-Statements ausführen
      val createSchemaFuture = db.run(sqlu"#$schemaSql")

      // Warten, bis die Tabellen erstellt sind
      Try(Await.result(createSchemaFuture, 10.seconds)) match {
        case Success(_) => println("Tabellen erfolgreich erstellt")
        case Failure(ex) => println(s"Fehler beim Erstellen der Tabellen: ${ex.getMessage}")
      }
    }

    db.close()
  }

  def main(args: Array[String]): Unit = {
    initializeTables()

    val MainBehavior = Behaviors.setup[Nothing] { context =>
      // Sensoren erstellen
      val rosenheim = context.spawn(Sensor("rosenheim"), "sensor-rosenheim")
      val rio = context.spawn(Sensor("rio"), "sensor-rio")
      val newYork = context.spawn(Sensor("new-york"), "sensor-new-york")

      // Datenansicht erstellen
      val dataView = context.spawn(SensorDataView(), "data-view")

      // Zeitplaner, der zufällige Temperaturwerte erzeugt
      implicit val executionContext = context.executionContext
      context.system.scheduler.scheduleAtFixedRate(1.second, 2.seconds) { () =>
        val random = new Random()

        // Temperatur zwischen -5 und 30 Grad
        val rosenheimTemp = -5 + random.nextDouble() * 35
        val rioTemp = -5 + random.nextDouble() * 35
        val newYorkTemp = -5 + random.nextDouble() * 35

        // Sende Werte an Sensoren
        rosenheim ! Sensor.RecordTemperature(rosenheimTemp, context.system.ignoreRef)
        rio ! Sensor.RecordTemperature(rioTemp, context.system.ignoreRef)
        newYork ! Sensor.RecordTemperature(newYorkTemp, context.system.ignoreRef)

        println(f"Gesendet - Rosenheim: $rosenheimTemp%.1f°C, Rio: $rioTemp%.1f°C, New York: $newYorkTemp%.1f°C")
      }

      Behaviors.empty
    }

    // Startet das Hauptsystem
    val system = ActorSystem(MainBehavior, "sensor-system")

    // Damit die Anwendung weiterläuft
    Thread.sleep(60000) // 1 Minute laufen lassen
    system.terminate()
  }
}