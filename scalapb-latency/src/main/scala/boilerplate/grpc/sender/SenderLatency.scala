package boilerplate.grpc.sender

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ExecutorService, Executors}

import boilerplate.grpc.protos._
import boilerplate.grpc.protos.echo._
import boilerplate.grpc.util.{Data, Metrics, Settings}
import com.google.protobuf.ByteString
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import io.grpc.{ClientInterceptors, ManagedChannel, ManagedChannelBuilder}
import io.grpc.stub.StreamObserver

import scala.concurrent.duration._

object SenderLatency  {
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
        pool.execute(new SenderLatency(channel, ep:Data.Endpoint))
      })
    } finally {
      pool.shutdown()
    }
  }

  def stop() {
    pool.shutdown()
  }

}

class SenderLatency(ch: ManagedChannel, ep:Data.Endpoint) extends Runnable {

  val logger     = Logger("boilerplate.grpc")

  val atomicCounter = new AtomicInteger(0)

  val latencyMetric = Metrics.timer(s"latency_${ep.label}")
  val counterEx = Metrics.counter(s"exception_${ep.label}")

  def createStream(ch: ManagedChannel): StreamObserver[MessageReq] = {
    //val interceptor = new HeaderClientInterceptor
    //val channel = ClientInterceptors.intercept(ch, interceptor)
    val channel = ch
    logger.debug("StreamObserver[MessageRes] - Create new stream")
    //.withOption(CallOptions.Key.of("waitForReady",true),true)
    EchoGrpc.stub(channel).bidirectionalStream(new StreamObserver[MessageRes] {
      override def onError(t: Throwable): Unit = {
        counterEx.inc()
        logger.error(s"StreamObserver[MessageRes] onError - ${t.getMessage} ${t.getStackTrace.toString}")
        atomicCounter.incrementAndGet()
      }

      override def onCompleted(): Unit = {
        logger.error(s"StreamObserver[MessageRes] onCompleted")
        atomicCounter.incrementAndGet()
        //doneSignal.countDown()
      }

      override def onNext(value: MessageRes): Unit = {
        val latency = System.nanoTime() - value.ts
        latencyMetric.update(latency, NANOSECONDS)
        logger.debug(s"Get a message - Id: ${value.messageCount} - Latency: ${latency}")
      }
    })
  }


  def run(): Unit = {
    var messageRequest:Option[StreamObserver[MessageReq]] = None
    var id = 0L
    while (true) {
      try {

        val payload = ByteString.copyFrom(Array.fill[Byte](ep.payload)(0))
        if (atomicCounter.get() > 0 || messageRequest.isEmpty) {
          messageRequest = Some(createStream(ch))
          atomicCounter.set(0)
        }
        Seq.range(0, ep.requests).foreach { _ =>
          id += 1
          val ts = System.nanoTime()
          logger.debug(s"Send a message - Ts: ${ts} - Id: $id")
          messageRequest.get.onNext(MessageReq(ts, id, 0, payload))
        }
        Thread.sleep(ep.interval)
      } catch {
        case e: RuntimeException =>
          logger.error(s"StreamObserver[MessageReq] catch RuntimeException - ${e.getMessage} ${e.getStackTrace.toString}")
        case e: Throwable =>
          logger.error("Exception unknown - Exit")
          System.exit(10)
      }
    }
  }
}
