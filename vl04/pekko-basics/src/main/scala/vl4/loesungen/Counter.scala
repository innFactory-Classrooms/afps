package vl4.loesungen

import org.apache.pekko.actor.typed.scaladsl.AskPattern.*
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import org.apache.pekko.util.Timeout

import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Counter {
  // Nachrichten-Protokoll
  sealed trait Command
  case object Increment extends Command
  case object Decrement extends Command
  case class GetValue(replyTo: ActorRef[Response]) extends Command
  
  // Antwort-Nachricht
  sealed trait Response
  case class CurrentValue(value: Int) extends Response
  
  // Actor-Verhalten mit Counter-Zustand
  def apply(count: Int = 0): Behavior[Command] = Behaviors.receive { (context, message) =>
    message match {
      case Increment =>
        println(s"Zähler wird auf ${count + 1} erhöht")
        apply(count + 1)
      case Decrement =>
        println(s"Zähler wird auf ${count - 1} verringert")
        apply(count - 1)
      case GetValue(replyTo) =>
        println(s"Aktueller Wert: $count")
        replyTo ! CurrentValue(count)
        Behaviors.same
    }
  }
}