/*package vl5.tpl

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.persistence.jdbc.query.scaladsl.JdbcReadJournal
import org.apache.pekko.persistence.query.{EventEnvelope, PersistenceQuery}
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Sink

object SensorDataView {

  sealed trait Command extends Serializable
  private case class NewEvent(envelope: EventEnvelope) extends Command

  // ANSI-Farbcodes
  private val RESET = "\u001B[0m"
  private val RED = "\u001B[31m"
  private val YELLOW = "\u001B[33m"
  private val BLUE = "\u001B[34m"

  def apply(): Behavior[Command] = {
    Behaviors.setup { context =>
      implicit val system = context.system.classicSystem
      implicit val materializer = Materializer(system)

      // über einen PersistenceQuery den ReadJournal abfragen
      // alle Events mit dem Tag "sensor" abfragen unnd über collect filtern ob es ein Sensor.TemperatureRecorded ist
      // dann eine NewEvent Nachricht an den Actor schicken, damit er die Nachricht verarbeiten kann

      handle()
    }
  }

  private def handle(): Behavior[Command] = {
    Behaviors.receiveMessage {
      case NewEvent(envelope) =>
        envelope.event match {
          case e: Sensor.TemperatureRecorded =>
            // Farbcode basierend auf Temperatur
            val color = if (e.value >= 20) RED else if (e.value >= 0) YELLOW else BLUE

            // Sensor-ID aus der Persistence-ID extrahieren (format: "sensor-<name>")
            val sensorId = envelope.persistenceId.stripPrefix("sensor-")

            // Temperatur auf 2 Dezimalstellen formatieren
            val formattedTemp = f"${e.value}%.2f"

            println(s"Temperatur in $sensorId: ${color}${formattedTemp}°C${RESET} bei ${e.timestamp}")
            Behaviors.same
          case _ =>
            Behaviors.same
        }
    }
  }
}*/