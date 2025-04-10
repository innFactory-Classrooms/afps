package vl4

import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import vl4.loesungen.Counter

class CounterTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Counter actor" should {
    "increment and decrement correctly" in {
      // Counter Actor erstellen
      val counter = spawn(Counter())

      // Probe für Antworten erstellen
      val probe = createTestProbe[Counter.Response]()

      // 5 mal inkrementieren
      (1 to 5).foreach(_ => counter ! Counter.Increment)

      // 2 mal dekrementieren
      (1 to 2).foreach(_ => counter ! Counter.Decrement)

      // Aktuellen Wert abfragen
      counter ! Counter.GetValue(probe.ref)

      // Prüfen, ob der Wert 3 ist (5 Inkremente - 2 Dekremente)
      val response = probe.receiveMessage()
      response should be(Counter.CurrentValue(3))
    }
  }
}