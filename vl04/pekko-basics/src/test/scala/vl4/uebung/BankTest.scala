package vl4.uebung

import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.*

class BankTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  import Bank.*

  "Ein Bank-System" should {
    "Überweisungen über mehrere Konten hinweg korrekt verarbeiten" in {
      // Probe für Antworten erstellen
      val probe = createTestProbe[KontoEröffnet]()
      val kontostandProbe = createTestProbe[Kontostand]()
      val überweisungsProbe = createTestProbe[ÜberweisungsAntwort]()

      // Bank erstellen
      val bank = spawn(Bank())

      // 3 Konten eröffnen, nur Konto 1 hat Guthaben
      bank ! EröffneKonto("Konto 1", 1000.0, 0.0, probe.ref)
      val konto1 = probe.receiveMessage().kontoRef

      bank ! EröffneKonto("Konto 2", 0.0, 0.0, probe.ref)
      val konto2 = probe.receiveMessage().kontoRef

      bank ! EröffneKonto("Konto 3", 0.0, 0.0, probe.ref)
      val konto3 = probe.receiveMessage().kontoRef

      // Überprüfe Startguthaben
      konto1 ! KontostandAbfragen(kontostandProbe.ref)
      kontostandProbe.expectMessage(Kontostand("Konto 1", 1000.0))

      konto2 ! KontostandAbfragen(kontostandProbe.ref)
      kontostandProbe.expectMessage(Kontostand("Konto 2", 0.0))

      konto3 ! KontostandAbfragen(kontostandProbe.ref)
      kontostandProbe.expectMessage(Kontostand("Konto 3", 0.0))

      // Überweisung von Konto 1 -> Konto 2
      konto1 ! Überweisen(1000.0, konto2, überweisungsProbe.ref)
      val transaktion1 = überweisungsProbe.expectMessageType[ÜberweisungGestartet]

      // Bestätige Empfang
      konto2 ! BestätigeEmpfang(transaktion1.transaktionId)

      // Überweisung von Konto 2 -> Konto 3
      konto2 ! Überweisen(1000.0, konto3, überweisungsProbe.ref)
      val transaktion2 = überweisungsProbe.expectMessageType[ÜberweisungGestartet]

      // Bestätige Empfang
      konto3 ! BestätigeEmpfang(transaktion2.transaktionId)

      // Überprüfe Endguthaben
      konto1 ! KontostandAbfragen(kontostandProbe.ref)
      kontostandProbe.expectMessage(Kontostand("Konto 1", 0.0))

      konto2 ! KontostandAbfragen(kontostandProbe.ref)
      kontostandProbe.expectMessage(Kontostand("Konto 2", 0.0))

      konto3 ! KontostandAbfragen(kontostandProbe.ref)
      kontostandProbe.expectMessage(Kontostand("Konto 3", 1000.0))
    }

    "Überweisungsgrenzen mit Dispokredit korrekt berücksichtigen" in {
      // Probes
      val probe = createTestProbe[KontoEröffnet]()
      val kontostandProbe = createTestProbe[Kontostand]()
      val überweisungsProbe = createTestProbe[ÜberweisungsAntwort]()

      // Bank erstellen
      val bank = spawn(Bank())

      // Zwei Konten mit Dispokredit eröffnen
      bank ! EröffneKonto("Konto 1", 100.0, 200.0, probe.ref)
      val konto1 = probe.receiveMessage().kontoRef

      bank ! EröffneKonto("Konto 2", 0.0, 0.0, probe.ref)
      val konto2 = probe.receiveMessage().kontoRef

      // Zu hohe Überweisung versuchen (300 + 200 Dispo = 500 möglich, 600 ist zu viel)
      konto1 ! Überweisen(600.0, konto2, überweisungsProbe.ref)
      überweisungsProbe.expectMessageType[ÜberweisungFehlgeschlagen]

      // Kontostand sollte unverändert sein
      konto1 ! KontostandAbfragen(kontostandProbe.ref)
      kontostandProbe.expectMessage(Kontostand("Konto 1", 100.0))

      konto2 ! KontostandAbfragen(kontostandProbe.ref)
      kontostandProbe.expectMessage(Kontostand("Konto 2", 0.0))

      // Jetzt Überweisung innerhalb des Dispolimits
      konto1 ! Überweisen(250.0, konto2, überweisungsProbe.ref)
      val transaktion = überweisungsProbe.expectMessageType[ÜberweisungGestartet]

      // Bestätige Empfang
      konto2 ! BestätigeEmpfang(transaktion.transaktionId)

      // Überprüfe Endguthaben (Konto 1 ist im Dispo)
      konto1 ! KontostandAbfragen(kontostandProbe.ref)
      kontostandProbe.expectMessage(Kontostand("Konto 1", -150.0))

      konto2 ! KontostandAbfragen(kontostandProbe.ref)
      kontostandProbe.expectMessage(Kontostand("Konto 2", 250.0))
    }

    "Einfache Überweisung zwischen zwei Konten korrekt durchführen" in {
      // Probes
      val probe = createTestProbe[KontoEröffnet]()
      val kontostandProbe = createTestProbe[Kontostand]()
      val überweisungsProbe = createTestProbe[ÜberweisungsAntwort]()

      // Bank erstellen
      val bank = spawn(Bank())

      // Zwei Konten eröffnen
      bank ! EröffneKonto("Alice", 1000.0, 0.0, probe.ref)
      val kontoAlice = probe.receiveMessage().kontoRef

      bank ! EröffneKonto("Bob", 0.0, 0.0, probe.ref)
      val kontoBob = probe.receiveMessage().kontoRef

      // Überprüfe Startguthaben
      kontoAlice ! KontostandAbfragen(kontostandProbe.ref)
      kontostandProbe.expectMessage(Kontostand("Alice", 1000.0))

      kontoBob ! KontostandAbfragen(kontostandProbe.ref)
      kontostandProbe.expectMessage(Kontostand("Bob", 0.0))

      // Überweisung von Alice zu Bob
      kontoAlice ! Überweisen(500.0, kontoBob, überweisungsProbe.ref)
      val transaktion = überweisungsProbe.expectMessageType[ÜberweisungGestartet]

      // Bestätige Empfang
      kontoBob ! BestätigeEmpfang(transaktion.transaktionId)

      // Überprüfe Endguthaben
      kontoAlice ! KontostandAbfragen(kontostandProbe.ref)
      kontostandProbe.expectMessage(Kontostand("Alice", 500.0))

      kontoBob ! KontostandAbfragen(kontostandProbe.ref)
      kontostandProbe.expectMessage(Kontostand("Bob", 500.0))
    }
  }
}