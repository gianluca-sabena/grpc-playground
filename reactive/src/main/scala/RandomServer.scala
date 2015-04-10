/*
 *
 * Author: gianluca.sabena@gmail.com
 *
 * Based on Google example https://github.com/grpc/grpc-java/tree/master/examples
 *
 */

package grpc.playground.random

import io.grpc.stub.StreamObserver
import io.grpc.transport.netty.NettyServerBuilder

class RandomNumberImpl extends RandomNumberGrpc.RandomNumber {
  val rnd = new scala.util.Random
  override def generateNumber(req: NumberRequest, responseObserver: StreamObserver[NumberResponse]) {
    //val reply: HelloResponse  = HelloResponse.newBuilder().setMessage("Hello " + req.getName).build();
    println(s" >>> client request ${req.getElements} random number(s) in range (${req.getMin},${req.getMax})")
    (1 to req.getElements).foreach(i => {
      val num = req.getMin + rnd.nextInt((req.getMax - req.getMin) + 1)
      val reply: NumberResponse = NumberResponse.newBuilder().setNum(num).setCount(i).setTotal(req.getElements).build()
      responseObserver.onValue(reply)
      //Thread.sleep(10)// simulate slow response
    })
    responseObserver.onCompleted()
  }
}

class RandomStringImpl extends RandomStringGrpc.RandomString {
  val rnd = new scala.util.Random
  override def generateString(req: StringRequest, responseObserver: StreamObserver[StringResponse]) {
    //val reply: HelloResponse  = HelloResponse.newBuilder().setMessage("Hello " + req.getName).build();
    (1 to req.getElements).foreach(i => {
      val str = rnd.nextString(req.getLength)
      val reply: StringResponse = StringResponse.newBuilder().setStr(str).setCount(i).setTotal(req.getElements).build()
      responseObserver.onValue(reply)
      //Thread.sleep(10) // simulate slow response
      println(s" >>> client request ${req.getElements} random string(s) with length (${req.getLength}}) - $i/${req.getElements} >>> ${reply.getStr}")
    })
    responseObserver.onCompleted()
  }
}

object RandomServer {
  def main(args: Array[String]): Unit = {
    val serverNumber = NettyServerBuilder.forPort(50053).addService(RandomNumberGrpc.bindService(new RandomNumberImpl())).build().start()
    val serverString = NettyServerBuilder.forPort(50054).addService(RandomStringGrpc.bindService(new RandomStringImpl())).build().start()
  }
}
