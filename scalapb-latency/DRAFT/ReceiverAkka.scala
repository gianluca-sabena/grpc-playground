package boilerplate.grpc.receiver

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Cancellable, Terminated}
import boilerplate.grpc.FullServerInterceptor
import boilerplate.grpc.protos._
import boilerplate.grpc.receiver.ReceiverSimple.logger
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.{LazyLogging, Logger}
import io.grpc.stub.StreamObserver
import io.grpc.{ServerBuilder, ServerInterceptors}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.language.postfixOps

case class GetMsg(msg:RandomReq, stream:StreamObserver[RandomRes])
case class SendMsg(msg:RandomReq, stream:StreamObserver[RandomRes])
case class GetCompleted(clientID:Option[String])

object ReceiverAkka {
  val conf = ConfigFactory.load()
  val system = ActorSystem("Sys",conf.getConfig("application.receiver"))
  val logger = Logger("boilerplate.akka.remote")
  val port = conf.getInt("application.sender.receiver.port")


  def rec(): Unit = {
    logger.info("Receiver initialized...")
    val actorReceiver = system.actorOf(Props[ReceiverAkka], "receiver")

    val service = new ServiceRandom(actorReceiver)


    val server = ServerBuilder
      .forPort(port)
//      .addService(ServerInterceptors.intercept(
//        RandomGrpc.bindService(service, ExecutionContext.global),
//        new FullServerInterceptor()))
      .addService(RandomGrpc.bindService(service, ExecutionContext.global))
      .asInstanceOf[ServerBuilder[_]]
      .build()

    server.start()
    logger.info(s"Server started, listening on $port")
    server.awaitTermination()

  }


  // Catch a Ctrl+C = sig term OR
  // In a shell run `# kill -TERM $pid`
  // mesos termination https://github.com/mesosphere/marathon/issues/4323
  scala.sys.addShutdownHook {
    println("Receiver - Terminating...")
    system.terminate()
    Await.result(system.whenTerminated, Duration.create(30, SECONDS))
    println("Receiver - Terminated... Bye")
  }
}


class ReceiverAkka extends Actor with ActorLogging{

  import ReceiverAkka._
  import system.dispatcher
  val random = scala.util.Random
  //system.logConfiguration()


  val schedulers = scala.collection.mutable.HashMap.empty[String,Cancellable]

  override def postStop(): Unit = {
    schedulers.foreach( e => e._2.cancel())
  }

  def receive = {
    case m: GetMsg =>
      log.info(s"Actor - GetMsg -  $m")
      val sch = context.system.scheduler.schedule(
        0 milliseconds,
        m.msg.interval milliseconds,
        self,
        SendMsg(m.msg, m.stream))
      schedulers+=(m.msg.clientId -> sch)
    case m: SendMsg =>
      m.stream.onNext(RandomRes(m.msg.clientId,m.msg.msg,0,random.nextInt(100)))
    case m: GetCompleted =>
      m.clientID match {
        case Some(id) =>
          log.info(s"Actor - Closed stream for  ClientID $id")
          schedulers.get(id).map { _.cancel() }
        case None =>
          log.info(s"Actor - Closed stream for undefined clientId")
      }
    case Terminated => {
      log.info("Receiver terminated")
      context.system.terminate()
    }

    case m:Any => {
      log.debug(s"get unknown msg: ${m}")
    }
  }
}

class ServiceRandom(actor:ActorRef) extends RandomGrpc.Random with LazyLogging {


  override def bidiStreamRandom(responseObserver: StreamObserver[RandomRes]) = {

    var clientID:Option[String] = None

    new StreamObserver[RandomReq] {

      var count:Long = 0

      override def onError(t: Throwable) = {
        logger.debug("bidiStreamRandom onCompleted - Ex message: "+t.getMessage)
        actor ! GetCompleted(clientID)
      }

      override def onCompleted() = {
        logger.debug("bidiStreamRandom onCompleted")
        //responseObserver.onCompleted()
        actor ! GetCompleted(clientID)
      }

      override def onNext(req: RandomReq) = {
        if (clientID.isEmpty) clientID = Some(req.clientId)
        logger.debug(s"bidiStreamRandom onNext - get RandomReq: ${req}")
        actor ! GetMsg(req,responseObserver)
      }
    }
  }
}
