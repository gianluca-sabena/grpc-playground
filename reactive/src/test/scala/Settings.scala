package grpc.playground.benchmark

import org.scalameter.api._

object Settings {

//  val streamElements = Gen.range("streamElements")(1500, 3000, 1500)
//  val streams = 5
//  val maxWarmupRuns = 1
//  val benchRuns = 1
//  val independentSamples = 1

  val streamElements = Gen.range("streamElements")(500, 1000, 500)
  val streams = 5
  val maxWarmupRuns = 3
  val benchRuns = 3
  val independentSamples = 3
}
