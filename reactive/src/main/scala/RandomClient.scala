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
    def isEven(n:Int):Boolean = n % 2 == 0
    def isOdd(n:Int):Boolean = n % 2 != 0

    val stream2Number = streamNumberA.filter(e => isEven(e.getNum)).zip(streamNumberB.filter(e => isOdd(e.getNum)))
    stream2Number.subscribe(e => println(s" (1) a tuple of 2 numbers: ${e._1.getNum} - ${e._2.getNum}"))
    stream2Number.foldLeft(0,0)((acc,e) => {
      (acc._1 + e._1.getNum, acc._2 + e._2.getNum)
    }).subscribe( e => println(s" sum left values: ${e._1} - right values: ${e._2} "))
  }
}