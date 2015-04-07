/*
 *
 * Author: gianluca.sabena@gmail.com
 *
 * Based on Google example https://github.com/grpc/grpc-java/tree/master/examples
 *
 */

package grpc.playground.helloworld

import io.grpc.stub.StreamObserver
import io.grpc.transport.netty.NettyServerBuilder


class GreeterImpl extends GreeterGrpc.Greeter {
  override def sayHello(req: HelloRequest ,  responseObserver: StreamObserver[HelloResponse]) {
    val reply: HelloResponse  = HelloResponse.newBuilder().setMessage("Hello " + req.getName).build();
    println(s" >>> client name >>> ${req.getName}")
    responseObserver.onValue(reply);
    responseObserver.onCompleted();
  }
}

object HelloWorldServer {
  def main( args:Array[String] ):Unit  = {
    val server = NettyServerBuilder.forPort(50051)
      .addService(GreeterGrpc.bindService(new GreeterImpl()))
      .build().start();
  }
}
