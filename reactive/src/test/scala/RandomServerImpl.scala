package grpc.playground.benchmark

import grpc.playground.random.{RandomStringImpl, RandomStringGrpc, RandomNumberImpl, RandomNumberGrpc}
import io.grpc.ServerImpl
import io.grpc.transport.netty.NettyServerBuilder

/**
 * Created by gianluca on 09/04/15.
 */
class RandomServerImpl {
  var serverNumber: ServerImpl = _
  var serverString: ServerImpl = _

  def startServer() = {
    serverNumber = NettyServerBuilder.forPort(50053).addService(RandomNumberGrpc.bindService(new RandomNumberImpl())).build().start()
    serverString = NettyServerBuilder.forPort(50054).addService(RandomStringGrpc.bindService(new RandomStringImpl())).build().start()
  }

  def stopServer = {
    serverNumber.shutdown()
    serverString.shutdown()
  }
}