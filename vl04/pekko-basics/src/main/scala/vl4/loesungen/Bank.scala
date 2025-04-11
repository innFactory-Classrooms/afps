package vl4.loesungen

import org.apache.pekko.actor.typed.scaladsl.{ActorContext, Behaviors}
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}


object Bank {

  // Protokoll - Nachrichten die unser Actor versteht
  sealed trait Command

  // Konto eröffnen
  final case class EröffneKonto(name: String, startguthaben: Double, dispokredit: Double, replyTo: ActorRef[KontoEröffnet]) extends Command
  final case class KontoEröffnet(kontoRef: ActorRef[KontoCommand])

  // Hauptactor - erstellt Konten
  def apply(): Behavior[Command] = Behaviors.receiveMessage { case msg =>
    Behaviors.setup { context =>
      msg match {
        case EröffneKonto(name, startguthaben, dispokredit, replyTo) =>
          println(s"[BANK] 🏦 Neues Konto wird eröffnet für $name mit Startguthaben €$startguthaben und Dispokredit €$dispokredit")
          val kontoRef = context.spawnAnonymous(Konto(name, startguthaben, dispokredit))
          replyTo ! KontoEröffnet(kontoRef)
          Behaviors.same
      }
    }
  }

  // Konto-Protokoll
  sealed trait KontoCommand

  // Transaktionen
  final case class Einzahlen(betrag: Double) extends KontoCommand
  final case class Auszahlen(betrag: Double, replyTo: ActorRef[AuszahlungsAntwort]) extends KontoCommand
  final case class Überweisen(betrag: Double, empfänger: ActorRef[KontoCommand], replyTo: ActorRef[ÜberweisungsAntwort]) extends KontoCommand
  final case class EmpfangeGeld(betrag: Double, absender: String, transaktionId: Long) extends KontoCommand
  final case class BestätigeEmpfang(transaktionId: Long) extends KontoCommand
  final case class KontostandAbfragen(replyTo: ActorRef[Kontostand]) extends KontoCommand

  // Antworten
  sealed trait AuszahlungsAntwort
  case class AuszahlungErfolg() extends AuszahlungsAntwort
  case class AuszahlungFehlgeschlagen(grund: String) extends AuszahlungsAntwort

  sealed trait ÜberweisungsAntwort
  case class ÜberweisungGestartet(transaktionId: Long) extends ÜberweisungsAntwort
  case class ÜberweisungFehlgeschlagen(grund: String) extends ÜberweisungsAntwort

  final case class Kontostand(name: String, betrag: Double)

  // Interne Zustandsinformationen
  private case class KontoZustand(
                                   name: String,
                                   guthaben: Double,
                                   dispokredit: Double,
                                   ausstehendeBestätigungen: Map[Long, (String, Double)], // TransaktionId -> (Absender, Betrag)
                                   nächsteTransaktionId: Long = 1
                                 )

  // Konto-Actor
  def Konto(name: String, startguthaben: Double, dispokredit: Double): Behavior[KontoCommand] = {
    Behaviors.setup { context =>
      println(s"[KONTO] ✅ Konto für $name mit Startguthaben €$startguthaben und Dispokredit €$dispokredit erstellt")
      aktivesKonto(KontoZustand(name, startguthaben, dispokredit, Map.empty))
    }
  }

  private def aktivesKonto(zustand: KontoZustand): Behavior[KontoCommand] = {
    Behaviors.receive { (context, nachricht) =>
      nachricht match {
        case Einzahlen(betrag) =>
          println(s"[EINZAHLUNG] 💰 ${zustand.name}: Einzahlung von €$betrag - Neuer Kontostand: €${zustand.guthaben + betrag}")
          aktivesKonto(zustand.copy(guthaben = zustand.guthaben + betrag))

        case Auszahlen(betrag, replyTo) =>
          if (zustand.guthaben - betrag >= -zustand.dispokredit) {
            println(s"[AUSZAHLUNG] 💸 ${zustand.name}: Auszahlung von €$betrag - Neuer Kontostand: €${zustand.guthaben - betrag}")
            replyTo ! AuszahlungErfolg()
            aktivesKonto(zustand.copy(guthaben = zustand.guthaben - betrag))
          } else {
            println(s"[AUSZAHLUNG] ❌ ${zustand.name}: Auszahlung von €$betrag fehlgeschlagen - Nicht genug Guthaben")
            replyTo ! AuszahlungFehlgeschlagen(s"Nicht genug Guthaben/Dispokredit")
            Behaviors.same
          }

        case Überweisen(betrag, empfänger, replyTo) =>
          if (zustand.guthaben - betrag >= -zustand.dispokredit) {
            val transaktionId = zustand.nächsteTransaktionId
            println(s"[ÜBERWEISUNG] ➡️ ${zustand.name}: Überweise €$betrag - TransaktionId: $transaktionId")

            // Sende Geld zum Empfänger
            empfänger ! EmpfangeGeld(betrag, zustand.name, transaktionId)

            // Reduziere Guthaben und merke die Transaktion
            replyTo ! ÜberweisungGestartet(transaktionId)
            aktivesKonto(zustand.copy(
              guthaben = zustand.guthaben - betrag,
              nächsteTransaktionId = transaktionId + 1
            ))
          } else {
            println(s"[ÜBERWEISUNG] ❌ ${zustand.name}: Überweisung von €$betrag fehlgeschlagen - Nicht genug Guthaben")
            replyTo ! ÜberweisungFehlgeschlagen("Nicht genug Guthaben/Dispokredit")
            Behaviors.same
          }

        case EmpfangeGeld(betrag, absender, transaktionId) =>
          println(s"[EMPFANG] ⬅️ ${zustand.name}: Empfange €$betrag von $absender - TransaktionId: $transaktionId (ausstehend)")
          // Speichere die ausstehende Bestätigung und erhöhe nicht sofort das Guthaben (Variante B)
          val neuerZustand = zustand.copy(
            ausstehendeBestätigungen = zustand.ausstehendeBestätigungen + (transaktionId -> (absender, betrag))
          )
          aktivesKonto(neuerZustand)

        case BestätigeEmpfang(transaktionId) =>
          zustand.ausstehendeBestätigungen.get(transaktionId) match {
            case Some((absender, betrag)) =>
              println(s"[BESTÄTIGUNG] ✓ ${zustand.name}: Bestätige Empfang von €$betrag von $absender - TransaktionId: $transaktionId")
              // Jetzt erst wird das Geld gutgeschrieben
              val neuerZustand = zustand.copy(
                guthaben = zustand.guthaben + betrag,
                ausstehendeBestätigungen = zustand.ausstehendeBestätigungen - transaktionId
              )
              println(s"[GUTSCHRIFT] 💰 ${zustand.name}: €$betrag gutgeschrieben - Neuer Kontostand: €${neuerZustand.guthaben}")
              aktivesKonto(neuerZustand)

            case None =>
              println(s"[BESTÄTIGUNG] ⚠️ ${zustand.name}: Keine ausstehende Transaktion mit ID $transaktionId gefunden")
              Behaviors.same
          }

        case KontostandAbfragen(replyTo) =>
          println(s"[ABFRAGE] 📊 ${zustand.name}: Aktueller Kontostand: €${zustand.guthaben}")
          replyTo ! Kontostand(zustand.name, zustand.guthaben)
          Behaviors.same
      }
    }
  }
}