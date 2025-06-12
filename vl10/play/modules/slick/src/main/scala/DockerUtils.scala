import scala.sys.process.{stringSeqToProcess, stringToProcess, ProcessLogger}

object DockerUtils {

  private val extensions = Seq("pg_trgm")

  def runMariaDbContainer(version: String): (String, Int) = {
    println(s"Starting mariadb:$version")
    val mariaDbDockerCommand = Seq(
      "docker",
      "run",
      "-e",
      "MARIADB_ROOT_PASSWORD=test",
      "-e",
      "MARIADB_USER=test",
      "-e",
      "MARIADB_PASSWORD=test",
      "-e",
      "MARIADB_DATABASE=test",
      "-p",
      "0:3306",
      "-d",
      s"mariadb:$version"
    )
    val containerId = mariaDbDockerCommand.!!.trim
    val dockerPortCommand = s"docker port $containerId 3306"
    val portMapping = dockerPortCommand.!!.trim
    val port = portMapping.split(":").last.toInt

    try {
      println(s"Waiting for MariaDB to start")
      var time = 100
      var elapsed = 0
      Thread.sleep(time)
      while (
        !s"docker logs $containerId"
          .!!(ProcessLogger(_ => ()))
          .contains("MariaDB init process done")
      ) {
        time = Math.min(time * 2, 1000)
        Thread.sleep(time)
        elapsed += time

        if (elapsed > 10000) {
          println(s"MariaDB failed to start")
          removeContainer(containerId)
          System.exit(1)
        }
      }
      Thread.sleep(250)
      println(s"MariaDB started")
    } catch {
      case _: Exception =>
        println(s"MariaDB failed to start")
        removeContainer(containerId)
        System.exit(1)
    }
    (containerId, port)
  }

  def runPgContainer(version: String): (String, Int) = {
    println(s"Starting postgres:$version")
    val postgresDockerCommand = Seq(
      "docker",
      "run",
      "-e",
      "POSTGRES_PASSWORD=test",
      "-e",
      "POSTGRES_USER=test",
      "-e",
      "POSTGRES_DB=test",
      "-p",
      "0:5432",
      "-d",
      s"postgres:$version"
    )
    val containerId = postgresDockerCommand.!!.trim
    val dockerPortCommand = s"docker port $containerId 5432"
    val portMapping = dockerPortCommand.!!.trim
    val port = portMapping.split(":").last.toInt

    try {
      println(s"Waiting for postgres to start")
      var time = 100
      var elapsed = 0
      Thread.sleep(time)
      while (
        !s"docker logs $containerId"
          .!!(ProcessLogger(_ => ()))
          .contains("database system is ready to accept connections")
      ) {
        time = Math.min(time * 2, 1000)
        Thread.sleep(time)
        elapsed += time

        if (elapsed > 10000) {
          println(s"Postgres failed to start")
          removeContainer(containerId)
          System.exit(1)
        }
      }
      Thread.sleep(250)
      println(s"Postgres started, creating extensions")
      extensions.foreach { extension =>
        s"docker exec $containerId psql -U test -d test -c 'CREATE EXTENSION IF NOT EXISTS $extension;'".!!
      }
    } catch {
      case _: Exception =>
        println(s"Postgres failed to start")
        removeContainer(containerId)
        System.exit(1)
    }
    println(s"Extensions created")
    (containerId, port)
  }

  def removeContainer(containerId: String): Unit = {
    s"docker rm -vf $containerId".!!
    println(s"Removed container $containerId")
  }
}
