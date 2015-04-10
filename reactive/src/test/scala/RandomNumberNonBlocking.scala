package grpc.playground.benchmark


import org.scalameter.api._


object RandomNumberNonBlocking extends PerformanceTest {

  lazy val executor = SeparateJvmsExecutor(
    new Executor.Warmer.Default,
    Aggregator.min,
    new Measurer.Default
  )
  lazy val reporter = new LoggingReporter
  lazy val persistor = Persistor.None

  var randomStream: RandomStream = new RandomStream

  performance of "grpcClient" in {
    measure method "getRandomNumberStreamNonBlocking" config(
      exec.maxWarmupRuns -> Settings.maxWarmupRuns,
      exec.benchRuns -> Settings.benchRuns,
      exec.independentSamples -> Settings.independentSamples) in {
      using(Settings.streamElements)  in { e =>
        randomStream.getRandomNumberStreamNonBlocking(e,Settings.streams).doOnCompleted(sys.exit(0)).subscribe(s => println(s" Data received non blocking: $s"))
      }
    }
  }

}