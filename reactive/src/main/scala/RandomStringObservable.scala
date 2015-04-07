package grpc.playground.random


import io.grpc.stub.StreamObserver
import io.grpc.transport.netty.{NegotiationType, NettyChannelBuilder}
import rx.lang.scala.Observable

/**
 * Created by gianluca on 03/04/15.
 */
object RandomStringObservable {
  val channelRandomString = NettyChannelBuilder.forAddress("localhost", 50054).negotiationType(NegotiationType.PLAINTEXT).build()
  def nonBlocking(): Observable[StringResponse] = {

    Observable(
      /*
       * This 'call' method will be invoked when the Observable is subscribed to.
       *
       * It spawns a thread to do it asynchronously.
       */

      subscriber => {
        // For simplicity this example uses a Thread instead of an ExecutorService/ThreadPool
        new Thread(new Runnable() {
          val asyncStub = RandomStringGrpc.newStub(channelRandomString)
          val stringRequest = StringRequest.newBuilder().setLength(10).setElements(10).build()
          def run() {
            asyncStub.generateString(stringRequest, new StreamObserver[StringResponse] {
              override def onError(t: Throwable): Unit = {
                println(s"error - $t")
                subscriber.onError(t)
              }
              override def onCompleted(): Unit = {
                //println("complete")
                subscriber.onCompleted()
              }
              override def onValue(value: StringResponse) = {
                //println(s"value string - ${value.getStr}")
                subscriber.onNext(value)
              }
            })
          }
        }).start()
      }
    )
  }
}
