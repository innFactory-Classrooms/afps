package vl4.loesungen

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.AskPattern.*
import org.apache.pekko.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.util.{Failure, Success}

object BankDemo extends App {
  import Bank.*

  // Actor System erstellen
  val system = ActorSystem(Bank(), "BankSystem")

  implicit val timeout: Timeout = Timeout(5.seconds)
  implicit val scheduler: org.apache.pekko.actor.typed.Scheduler = system.scheduler

  // Zwei Konten eröffnen
  val konto1Future = system.ask[KontoEröffnet](ref => EröffneKonto("Max Mustermann", 1000.0, 500.0, ref))
  val konto2Future = system.ask[KontoEröffnet](ref => EröffneKonto("Erika Musterfrau", 500.0, 200.0, ref))

  // Nach Eröffnung eine Überweisung durchführen
  for {
    konto1Ref <- konto1Future.map(_.kontoRef)
    konto2Ref <- konto2Future.map(_.kontoRef)
  } {
    // Kontostand vor Überweisung abfragen
    konto1Ref ! KontostandAbfragen(system.ignoreRef)
    konto2Ref ! KontostandAbfragen(system.ignoreRef)

    // Überweisung durchführen
    konto1Ref.ask[ÜberweisungsAntwort](ref => Überweisen(500.0, konto2Ref, ref)).onComplete {
      case Success(ÜberweisungGestartet(id)) =>
        println(s"Überweisung mit ID $id gestartet")
        // Nach kurzer Zeit die Überweisung bestätigen (simuliert Empfänger-Bestätigung)
        system.scheduler.scheduleOnce(1.second, () => {
          konto2Ref ! BestätigeEmpfang(id)
        })

      case Success(ÜberweisungFehlgeschlagen(grund)) =>
        println(s"Überweisung fehlgeschlagen: $grund")

      case Failure(exception) =>
        println(s"Fehler bei Überweisung: ${exception.getMessage}")
    }

    // Kontostand nach der Überweisung abfragen
    system.scheduler.scheduleOnce(2.seconds, () => {
      konto1Ref ! KontostandAbfragen(system.ignoreRef)
      konto2Ref ! KontostandAbfragen(system.ignoreRef)

      // Warten, damit auch alles fertig verarbeitet wurde.
      system.scheduler.scheduleOnce(5.second, () => system.terminate())
    })
  }
}