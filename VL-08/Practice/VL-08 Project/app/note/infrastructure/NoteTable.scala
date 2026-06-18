package note.infrastructure

import slick.jdbc.H2Profile.api.*
import note.domain.models.Note

/** Slick-Tabellendefinition fuer die Domain "Note".
  *
  * Eine Table-Klasse beschreibt das Mapping zwischen den Spalten der
  * SQL-Tabelle "notes" und der Case Class [[Note]]. Slick generiert daraus
  * typsichere Queries (filter/map/sortBy ...).
  */
class NoteTable(tag: Tag) extends Table[Note](tag, "notes") {

  // Spalten: Name + Typ + optionale Column-Options
  def id    = column[Long]("id", O.PrimaryKey, O.AutoInc) // Primaerschluessel, auto-increment
  def title = column[String]("title", O.Length(255))      // VARCHAR(255)
  def done  = column[Boolean]("done", O.Default(false))   // DEFAULT false

  // Default-Projektion: Spalten-Tupel <-> Note (Reihenfolge muss zur Case Class passen)
  def * = (id, title, done).mapTo[Note]

  // Optionaler Index (zur Demonstration des Table-DSL)
  def titleIdx = index("idx_note_title", title)
}

object NoteTable {
  // Einstiegspunkt fuer Queries auf der Tabelle
  val notes = TableQuery[NoteTable]
}
