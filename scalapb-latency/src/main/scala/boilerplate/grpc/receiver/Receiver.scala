package boilerplate.grpc.receiver

//import boilerplate.grpc.FullServerInterceptor

import boilerplate.grpc.protos.echo._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging._
import io.grpc.{ServerBuilder, ServerInterceptors}

import scala.concurrent.ExecutionContext

object ReceiverSimple extends LazyLogging {

  val conf = ConfigFactory.load()
  val port = conf.getInt("application.sender.receiver.port")

  val server = ServerBuilder
    .forPort(port)
    // .addService(ServerInterceptors.intercept(
    //   EchoGrpc.bindService(new ServiceEchoGrpc(), ExecutionContext.global),
    //   new FullServerInterceptor()))
    .addService(EchoGrpc.bindService(new ServiceEchoGrpc(), ExecutionContext.global))
    .asInstanceOf[ServerBuilder[_]]
    .build()

  def start() = {
    server.start()
    logger.info(s"Server started, listening on $port")
    server.awaitTermination()
  }

  def stop() = {
    server.shutdown()
  }
}




