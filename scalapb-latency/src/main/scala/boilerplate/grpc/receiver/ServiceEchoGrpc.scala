package boilerplate.grpc.receiver

//import boilerplate.grpc.protos._
import boilerplate.grpc.protos.echo._
import com.typesafe.scalalogging.LazyLogging
import io.grpc.stub.StreamObserver

class ServiceEchoGrpc extends EchoGrpc.Echo with LazyLogging {

  override def responseStream(
                               request: MessageReq,
                               responseObserver: StreamObserver[MessageRes]): Unit = {
    logger.debug(s"RECEIVER - responseStream - get a message - Message count: ${request.messageCount}")
    (1 to 10).foreach { v =>
      Thread.sleep(100)
      logger.debug(s"RECEIVER - responseStream - send back a message - Message count: ${request.messageCount}")
      responseObserver.onNext(
        MessageRes(request.ts,request.messageCount,request.burstCount,request.payload)
      )
    }
    responseObserver.onCompleted()
  }


  override def bidirectionalStream(responseObserver: StreamObserver[MessageRes]): StreamObserver[MessageReq] = {

    new StreamObserver[MessageReq] {

      var count:Long = 0

      override def onError(t: Throwable) = {
        logger.debug("RECEIVER - bidirectionalStream onCompleted - Ex message: "+t.getMessage)
      }

      override def onCompleted() = {
        logger.debug("RECEIVER - bidirectionalStream onCompleted")
        responseObserver.onCompleted()
      }

      override def onNext(value: MessageReq) = {
        responseObserver.onNext(MessageRes(value.ts,value.messageCount,value.burstCount,value.payload))
      }
    }
  }

 
}
