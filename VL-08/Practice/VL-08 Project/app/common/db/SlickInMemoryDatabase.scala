package common.db

import slick.jdbc.H2Profile.api.*
import note.infrastructure.NoteTable

import javax.inject.Singleton
import scala.concurrent.Await
import scala.concurrent.duration.*

/** Stellt eine In-Memory-Datenbank (H2) bereit, die über Slick angesprochen wird.
  *
  * DB_CLOSE_DELAY=-1 haelt die In-Memory-DB am Leben, solange die JVM laeuft.
  * Ohne diese Option wuerde H2 die DB verwerfen, sobald die letzte Verbindung
  * geschlossen wird.
  */
@Singleton
class SlickInMemoryDatabase {

  val db: Database = Database.forURL(
    url = "jdbc:h2:mem:vl08;DB_CLOSE_DELAY=-1",
    driver = "org.h2.Driver"
  )

  // Schema einmalig beim Start anlegen (die In-Memory-DB startet leer).
  // Blockierend, damit die Tabellen existieren, bevor das erste Repository sie nutzt.
  // Weitere Tabellen hier per ++ ergaenzen: (NoteTable.notes.schema ++ X.schema)
  Await.result(
    db.run(NoteTable.notes.schema.createIfNotExists),
    10.seconds
  )
}
