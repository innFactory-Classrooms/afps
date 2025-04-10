package vl4.loesungen

import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}

object HelloWorld {
  // Message protocols
  sealed trait MessageB
  case class Greeting(message: String) extends MessageB

  sealed trait MessageA
  case class SendGreeting(to: ActorRef[MessageB]) extends MessageA

  // Actor B: Prints received messages
  def receiverBehavior(): Behavior[MessageB] = Behaviors.receiveMessage {
    case Greeting(msg) =>
      println(s"Actor B: $msg")
      Behaviors.same
  }

  // Actor A: Sends message to B
  def senderBehavior(): Behavior[MessageA] = Behaviors.receiveMessage {
    case SendGreeting(to) =>
      to ! Greeting("Hello world")
      Behaviors.same
  }

  def main(args: Array[String]): Unit = {
    val system = ActorSystem[Nothing](Behaviors.setup { context =>
      val actorB = context.spawn(receiverBehavior(), "actor-b")
      val actorC = context.spawn(receiverBehavior(), "actor-c")
      val actorA = context.spawn(senderBehavior(), "actor-a")
      actorA ! SendGreeting(actorC)
      Behaviors.empty
    }, "HelloWorldSystem")

    Thread.sleep(500)
    system.terminate()
  }
}
