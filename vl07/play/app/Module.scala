import com.google.inject.AbstractModule
import slick.jdbc.JdbcBackend.Database

import java.time.Clock

/** This class is a Guice module that tells Guice how to bind several different types. This Guice module is created when the Play application starts.
  *
  * Play will automatically use any class called `Module` that is in the root package. You can create modules in other locations by adding `play.modules.enabled` settings to the `application.conf` configuration file.
  */
class Module extends AbstractModule {
  override def configure() = {
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)

    // FlywayMigrator direkt bei Start der Application instanziieren → Migrationen ausführen
    bind(classOf[FlywayMigrator]).asEagerSingleton()

    // Slick Database Instanz aus der Configuration erzeugen
    bind(classOf[Database]).toInstance(Database.forConfig("db.default"))
  }
}
