package boilerplate.grpc.sender


import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CountDownLatch, ExecutorService, Executors}

import boilerplate.grpc.protos._
import boilerplate.grpc.util.{HeaderClientInterceptor, Settings}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import io.grpc.stub.StreamObserver
import io.grpc.{ClientInterceptors, ManagedChannel, ManagedChannelBuilder}

object SenderBidiStreamRandom  {
  val logger = Settings.logger
  val pool: ExecutorService = Executors.newCachedThreadPool()
  val conf                  = ConfigFactory.load()

  val host = conf.getString("application.sender.receiver.host")
  val port = conf.getInt("application.sender.receiver.port")

  val channel = ManagedChannelBuilder
    .forAddress(host, port)
    .usePlaintext(true)
    .asInstanceOf[ManagedChannelBuilder[_]]
    .build()

  def run() {
    val random = scala.util.Random
    try {
      Settings.getEndpoints().map( ep => {
        Thread.sleep(random.nextInt(ep.interval * 10))
        pool.execute(new SenderBidiStreamRandom(channel))
      })
    } finally {
      pool.shutdown()
    }
  }

}

class SenderBidiStreamRandom(ch: ManagedChannel) extends Runnable {

  val logger     = Logger("boilerplate.grpc")

  val doneSignal = new CountDownLatch(1)

  def createStream(ch: ManagedChannel): StreamObserver[RandomReq] = {
    val interceptor = new HeaderClientInterceptor
    //val channel = ClientInterceptors.intercept(ch, interceptor) // add custom streamId
    val channel = ch
    logger.debug("StreamObserver[MessageRes] - Create new stream")
    RandomGrpc.stub(channel).bidiStreamRandom(new StreamObserver[RandomRes] {
      override def onError(t: Throwable): Unit = {
        logger.error(s"StreamObserver[MessageRes] onError - ${t.getMessage} ${t.getStackTrace.toString}")
        doneSignal.countDown()
      }

      override def onCompleted(): Unit = {
        logger.error(s"StreamObserver[MessageRes] onCompleted")
        doneSignal.countDown()
      }

      override def onNext(res: RandomRes): Unit = {
        logger.debug(s"Get a message - ${res}")
      }
    })
  }

  def run(): Unit = {
    val stream1 = createStream(ch)
    val stream2 = createStream(ch)
    try {
      logger.debug("Send 1 request")
      stream1.onNext(RandomReq("clientID-1","message-1",0,1000))
      stream2.onNext(RandomReq("clientID-2","message-1",0,1000))
      Thread.sleep(5000)
      stream2.onCompleted()
      Thread.sleep(15000)
      stream1.onCompleted()
      Thread.sleep(5000)
      //doneSignal.await()
    } catch {
      case e: RuntimeException =>
        logger.error(s"StreamObserver[MessageReq] catch RuntimeException - ${e.getMessage} ${e.getStackTrace.toString}")
      case e: Throwable =>
        logger.error("Exception unknown - Exit")
        System.exit(10)
    }
  }
}
