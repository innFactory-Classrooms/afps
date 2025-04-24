package vl5

import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.persistence.typed.PersistenceId
import org.apache.pekko.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

import java.time.Instant

trait Serializable

object Sensor {

  // Befehle, die der Actor empfangen kann
  sealed trait Command extends Serializable
  final case class RecordTemperature(value: Double, replyTo: ActorRef[Measurement]) extends Command
  final case class GetMeasurements(replyTo: ActorRef[MeasurementHistory]) extends Command

  // Ereignisse, die persistiert werden
  sealed trait Event extends Serializable
  final case class TemperatureRecorded(timestamp: Instant, value: Double) extends Event

  // Antwortnachrichten
  final case class Measurement(timestamp: Instant) extends Serializable
  final case class MeasurementHistory(measurements: List[TemperatureRecorded]) extends Serializable

  // Zustand des Actors
  final case class State(measurements: List[TemperatureRecorded]) extends Serializable {
    def recordTemperature(value: Double): State = {
      val event = TemperatureRecorded(Instant.now(), value)
      copy(measurements = event :: measurements)
    }
  }

  def apply(sensorId: String): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId(s"sensor-$sensorId"),
      emptyState = State(List.empty),
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )
      .withTagger(_ => Set("sensor")) // Tag für Abfragen hinzufügen
      .snapshotWhen((_, event, sequenceNr) =>
        sequenceNr % 10 == 0) // Snapshot nach jeder 10. Nachricht
  }

  private val commandHandler: (State, Command) => Effect[Event, State] = { (state, command) =>
    command match {
      case RecordTemperature(value, replyTo) =>
        val event = TemperatureRecorded(Instant.now(), value)
        Effect
          .persist(event)
          .thenReply(replyTo)(_ => Measurement(event.timestamp))

      case GetMeasurements(replyTo) =>
        Effect.reply(replyTo)(MeasurementHistory(state.measurements))
    }
  }

  private val eventHandler: (State, Event) => State = { (state, event) =>
    event match {
      case e: TemperatureRecorded => state.recordTemperature(e.value)
    }
  }
}