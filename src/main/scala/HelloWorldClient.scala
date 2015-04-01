/*
 *
 * Author: gianluca.sabena@gmail.com
 *
 * Based on Google example https://github.com/grpc/grpc-java/tree/master/examples
 *
 */

package grpc.playground.helloworld


import java.text.SimpleDateFormat
import java.util.Calendar

import io.grpc.transport.netty.{NegotiationType, NettyChannelBuilder}


import java.util.concurrent.TimeUnit


object HelloWorldClient {
  def main( args:Array[String] ):Unit  = {
    val format = new SimpleDateFormat("d-M-y HH:mm:ss")
    val date = format.format(Calendar.getInstance().getTime)
    val channel = NettyChannelBuilder.forAddress("localhost", 50051).negotiationType(NegotiationType.PLAINTEXT).build()
    val blockingStub = GreeterGrpc.newBlockingStub(channel)
    val request = HelloRequest.newBuilder().setName(s"Super Mario Bros ($date)").build()
    val response = blockingStub.sayHello(request)
    println(" >>> server >>> " + response.getMessage)
    channel.shutdown().awaitTerminated(5, TimeUnit.SECONDS)
  }
}

