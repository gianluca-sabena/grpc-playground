package boilerplate.grpc.util

import java.util.concurrent.TimeUnit

import com.codahale.metrics.jvm._
import com.codahale.metrics.{MetricRegistry, Timer => DropwizardTimer, _}
import org.slf4j.LoggerFactory

object Metrics {
  private def createMetric[T](name:String) = {

  }

  val metricRegistry = new MetricRegistry()

  metricRegistry.register("jvm.attribute", new JvmAttributeGaugeSet())
  metricRegistry.register("jvm.gc", new GarbageCollectorMetricSet())
  metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet())
  metricRegistry.register("jvm.threads", new ThreadStatesGaugeSet())

  val jmxReporter = JmxReporter.forRegistry(metricRegistry)
    .convertRatesTo(TimeUnit.SECONDS)
    .convertDurationsTo(TimeUnit.MILLISECONDS)
    .build()
  jmxReporter.start()

  def consoleReporterInit(interval:Long) = {
    val slf4jReporter = Slf4jReporter.forRegistry(metricRegistry)
      .outputTo(LoggerFactory.getLogger("boilerplate.grpc.metrics"))
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build()
    slf4jReporter.start(interval,TimeUnit.SECONDS)
  }

  def counter(id: String): Counter = {
    metricRegistry.counter(MetricRegistry.name("boilerplate", "grpc", "counter", id))
  }

  def meter(id: String): Meter = {
    metricRegistry.meter(MetricRegistry.name("boilerplate", "grpc", "meter", id))
  }

  def timer(id: String): DropwizardTimer = {
    metricRegistry.timer(MetricRegistry.name("boilerplate", "grpc", "timer", id))
  }
}
