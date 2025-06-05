import com.google.inject.{Inject, Singleton}
import org.flywaydb.core.Flyway
import play.api.Configuration

@Singleton
class FlywayMigrator @Inject() (config: Configuration) {
  // DB-Verbindungsdaten aus der Configuration lesen
  private val url: String  = config.get[String]("db.default.url")
  private val user: String = config.get[String]("db.default.username")
  private val pass: String = config.get[String]("db.default.password")

  // Flyway konfigurieren und Migrationen ausführen
  Flyway
    .configure()
    .dataSource(url, user, pass)
    .locations("classpath:db/migration")
    .load()
    .migrate()
}
