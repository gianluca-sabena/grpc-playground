package grpc.playground.random


import java.util.concurrent.TimeUnit

import io.grpc.stub.StreamObserver
import io.grpc.transport.netty.{NegotiationType, NettyChannelBuilder}
import rx.lang.scala.Observable

object RandomNumberObservable {
  val channelRandomNumber = NettyChannelBuilder.forAddress("localhost", 50053).negotiationType(NegotiationType.PLAINTEXT).build()
  def nonBlocking(): Observable[NumberResponse] = {

    Observable(
      /*
       * This 'call' method will be invoked when the Observable is subscribed to.
       *
       * It spawns a thread to do it asynchronously.
       */

      subscriber => {
        // For simplicity this example uses a Thread instead of an ExecutorService/ThreadPool
        new Thread(new Runnable() {
          val asyncStub = RandomNumberGrpc.newStub(channelRandomNumber)
          val numberRequest = NumberRequest.newBuilder().setMin(1).setMax(100).setElements(10).build()
          def run() {
            asyncStub.generateNumber(numberRequest, new StreamObserver[NumberResponse] {
              override def onError(t: Throwable): Unit = {
                println(s"error - $t")
                subscriber.onError(t)
              }
              override def onCompleted(): Unit = {
                //println("complete")
                subscriber.onCompleted()
              }

              override def onValue(value: NumberResponse) = {
                //println(s"value number s- ${value.getNum}")
                subscriber.onNext(value)
              }
            })

            //after sending all values we complete the sequence
//            if (!subscriber.isUnsubscribed) {
//              channelRandomNumber.shutdown().awaitTerminated(5, TimeUnit.SECONDS)
//              subscriber.onCompleted()
//            }
          }
        }).start()
      }
    )
  }
}
