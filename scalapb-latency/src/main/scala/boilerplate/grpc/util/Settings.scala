package boilerplate.grpc.util

import boilerplate.grpc.util.Data.Endpoint
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import net.ceedubs.ficus.Ficus._

object Settings {
  val config: Config = ConfigFactory.load()
  val logger = Logger(s"${getProject}.${getApp}")

  def getEndpoints() = {
    import net.ceedubs.ficus.readers.ArbitraryTypeReader._
    val endpoints = config.as[List[Endpoint]]("application.endpoints")
    endpoints map {e=> logger.debug(e.toString)}
    endpoints
  }

  def getProject = {
    config.as[String]("application.project")
  }

  def getApp = {
    config.as[String]("application.app")
  }

  // metrics
  if (config.as[Boolean]("application.metrics.console-reporter.enabled"))
    Metrics.consoleReporterInit(config.as[Long]("application.metrics.console-reporter.interval"))


}

