package vl4.uebung

import org.apache.pekko.actor.typed.scaladsl.{ActorContext, Behaviors}
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}

import scala.collection.immutable.Queue

object Bank {

  // Bank Protokoll - Nachrichten die unsere Bank verteht
  sealed trait Command
  
  // EröffneKonto extends Command
  // KontoEröffnet extends Command 

  def apply(): Behavior[Command] = Behaviors.receiveMessage { case msg =>
    Behaviors.setup { context =>
      msg match {
        //case EröffneKonto
        case _ => println("REMOVE ME")
          Behaviors.same
      }
    }
  }

  // Konto-Protokoll
  sealed trait KontoCommand

  // extends KontoCommand
  //Einzahlen
  //Auszahlen
  //Überweisen
  //EmpfangeGeld
  //BestätigeEmpfang
  //KontostandAbfragen

  // Antworten
  sealed trait AuszahlungsAntwort
  //AuszahlungErfolg
  //AuszahlungFehlgeschlagen

  //sealed trait ÜberweisungsAntwort
  //ÜberweisungGestartet
  //ÜberweisungFehlgeschlagen
  //ÜberweisungBestätigt

  //final case class Kontostand(name: String, betrag: Double)

  // Interne Zustandsinformationen
  private case class KontoZustand(
                                   name: String,
                                   guthaben: Double,
                                   dispokredit: Double,
                                   ausstehendeBestätigungen: Map[Long, (String, Double)], // TransaktionId -> (Absender, Betrag)
                                   nächsteTransaktionId: Long = 1
                                 )

  // Konto-Actor
  def Konto(name: String, startguthaben: Double, dispokredit: Double): Behavior[KontoCommand] = ???

  private def aktivesKonto(zustand: KontoZustand): Behavior[KontoCommand] = {
    Behaviors.receive { (context, nachricht) =>
      nachricht match {
        //case Einzahlen(betrag) =>
        //case Auszahlen(betrag, replyTo) =>
        //case Überweisen(betrag, empfänger, replyTo) =>
        //case EmpfangeGeld(betrag, absender, transaktionId) =>
        //case BestätigeEmpfang(transaktionId) =>
        //case KontostandAbfragen(replyTo) =>
        case _ => 
          println("REMOVE ME")
          Behaviors.same
      }
    }
  }
}