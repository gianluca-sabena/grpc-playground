package grpc.playground.benchmark

import grpc.playground.random.RandomNumberObservable


class RandomStream {
  //val streamString = RandomStringObservable.nonBlocking()
  def isEven(n: Int): Boolean = n % 2 == 0

  def isOdd(n: Int): Boolean = n % 2 != 0


  def getRandomNumberStreamNonBlocking(size: Int, streams:Int = 1 ) = {
    val s = (1 to streams).map { _ => RandomNumberObservable.nonBlocking(size)}
    val t = s.reduce((a,b) => a merge b)
    t.foldLeft(0)((tot,e)=>tot + e.getNum)
  }

  def getRandomNumberStreamBlocking(size: Int, streams:Int = 1 ) = {
    val s = (1 to streams).map { _ => RandomNumberObservable.blocking(size)}
    val t = s.reduce((a,b) => a merge b)
    t.foldLeft(0)((tot,e)=>tot + e.getNum)
  }

}