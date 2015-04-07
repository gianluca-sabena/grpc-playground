/*
 *
 * Author: gianluca.sabena@gmail.com
 *
 *
 */

package grpc.playground.random
//

object RandomClient {
  def main( args:Array[String] ):Unit  = {
    val streamNumberA = RandomNumberObservable.nonBlocking()//.subscribe(a => println(s" --- ${a.getNum}"))
    val streamNumberB = RandomNumberObservable.nonBlocking()
    val streamString = RandomStringObservable.nonBlocking()//.subscribe(a => println(s" --- ${a.getStr}"))
    val stream2Number = streamNumberA.zip(streamNumberB)
    stream2Number.subscribe(e => println(s" (1) a touple of 2 numbers: ${e._1.getNum} - ${e._2.getNum}"))
    stream2Number.subscribe(e => println(s" (2) a touple of 2 numbers: ${e._1.getNum} - ${e._2.getNum}"))
  }
}