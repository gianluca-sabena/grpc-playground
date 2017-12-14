package boilerplate.grpc.sender

import java.util.concurrent.{CountDownLatch, ExecutorService, Executors}

import boilerplate.grpc.protos._
import boilerplate.grpc.util.{Data, HeaderClientInterceptor, Metrics, Settings}
import com.google.protobuf.ByteString
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import io.grpc.stub.StreamObserver
import io.grpc.{ClientInterceptors, ManagedChannel, ManagedChannelBuilder}

import scala.concurrent.duration._

object SenderRandom  {
  //implicit val exec         = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  val pool: ExecutorService = Executors.newCachedThreadPool()
  val conf                  = ConfigFactory.load()

  val host = conf.getString("application.sender.receiver.host")
  val port = conf.getInt("application.sender.receiver.port")

  val channel = ManagedChannelBuilder
    .forAddress(host, port)
    .usePlaintext(true)
    .asInstanceOf[ManagedChannelBuilder[_]]
    .build()

  //val interceptor = new HeaderClientInterceptor()
  //val channel = ClientInterceptors.intercept(originChannel, interceptor)
  //val blockingStub = GreeterGrpc.newBlockingStub(channel);

  val logger = Settings.logger

  def run() {
    //Thread.sleep(3000)
    val random = scala.util.Random
    try {
      Settings.getEndpoints().map( ep => {
        Thread.sleep(random.nextInt(ep.interval * 10))
        pool.execute(new SenderRandom(channel, ep:Data.Endpoint))
      })
    } finally {
      pool.shutdown()
    }
  }

}

class SenderRandom(ch: ManagedChannel, ep:Data.Endpoint) extends Runnable {

  val logger     = Logger("boilerplate.grpc")

  val doneSignal = new CountDownLatch(1)

  val latencyMetric = Metrics.timer(s"latency_${ep.label}")
  val counterEx = Metrics.counter(s"exception_${ep.label}")

  def createStream(ch: ManagedChannel, request:MessageReq) = {
    val interceptor = new HeaderClientInterceptor
    val channel = ClientInterceptors.intercept(ch, interceptor)
    //val channel = ch
    logger.debug("StreamObserver[MessageRes] - Create new stream")
    //.withOption(CallOptions.Key.of("waitForReady",true),true)


    EchoGrpc.stub(channel).responseStream(request,new StreamObserver[MessageRes] {
      override def onError(t: Throwable): Unit = {
        counterEx.inc()
        logger.error(s"StreamObserver[MessageRes] onError - ${t.getMessage} ${t.getStackTrace.toString}")
        doneSignal.countDown()
      }

      override def onCompleted(): Unit = {
        logger.error(s"StreamObserver[MessageRes] onCompleted")
        doneSignal.countDown()
        //doneSignal.countDown()
      }

      override def onNext(value: MessageRes): Unit = {
        val latency = System.nanoTime() - value.ts
        latencyMetric.update(latency, NANOSECONDS)
        logger.debug(s"StreamObserver[MessageRes] onNext - Id: ${value.messageCount} - Latency: $latency")
        //Thread.sleep(5000)
        //logger.debug("Sleep end")
      }
    })
  }

  //TODO: how to handle client reconnection
  // see -  https://groups.google.com/forum/#!topic/grpc-io/-sbIwInGlqI

  def run(): Unit = {
    try {
      val ts = System.nanoTime()
      val payload = ByteString.copyFrom(Array.fill[Byte](ep.payload)(0))
      createStream(ch,MessageReq(ts, 0, 0, payload))
      Thread.sleep(3000)
      createStream(ch,MessageReq(ts, 1, 1, payload))

    } catch {
      case e: RuntimeException =>
        logger.error(s"StreamObserver[MessageReq] catch RuntimeException - ${e.getMessage} ${e.getStackTrace.toString}")
      case e: Throwable =>
        logger.error("Exception unknown - Exit")
        System.exit(10)
    }
    doneSignal.await()
  }
}
