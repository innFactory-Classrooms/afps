import javax.inject.{Inject, Provider, Singleton}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.*
import com.typesafe.config.Config
import play.api.{Configuration, Environment, Logging, Mode}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.PekkoGuiceSupport

class Module(environment: Environment, configuration: Configuration)
    extends AbstractModule
    with PekkoGuiceSupport
    with Logging {

  override def configure(): Unit = {
    logger.info(s"Configuring ${environment.mode}")
  }

}
