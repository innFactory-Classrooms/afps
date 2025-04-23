/*package vl5.tpl

import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.persistence.typed.PersistenceId
import org.apache.pekko.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

import java.time.Instant

trait Serializable

object Sensor {

  // Befehle, die der Actor empfangen kann
  sealed trait Command extends Serializable
  // RecordTemperature und GetMeasurements sind die Befehle, die der Actor empfangen kann.

  // Ereignisse, die persistiert werden
  sealed trait Event extends Serializable
  // TemperatureRecorded ist das Ereignis, das persistiert wird, wenn eine Temperatur aufgezeichnet wird.

  // Antwortnachrichten
  // Measurement und MeasurementHistory sind die Antwortnachrichten, die der Actor zurücksendet.ble

  // Zustand des Actors
  final case class State(measurements: List[TemperatureRecorded]) extends Serializable {
    def recordTemperature(value: Double): State = {
      val event = TemperatureRecorded(Instant.now(), value)
      copy(measurements = event :: measurements)
    }
  }

  def apply(sensorId: String): Behavior[Command] = {
    // EventSourcedBehavior Command, Event, State implementieren
    // Tag für Abfragen hinzufügen withTagger
    // Snapshot nach jeder 10. Nachricht
  }

  private val commandHandler: (State, Command) => Effect[Event, State] = { (state, command) =>
    command match {
      case RecordTemperature(value, replyTo) =>
        // Event vom Command erzeugen und persistieren

      case GetMeasurements(replyTo) =>
        // Antwort an den Sender zurücksenden
    }
  }

  private val eventHandler: (State, Event) => State = { (state, event) =>
    event match {
      // neuen State erzeugen
    }
  }
}*/