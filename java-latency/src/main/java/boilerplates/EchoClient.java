/*
 * Copyright 2015, gRPC Authors All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boilerplates;

import boilerplates.proto.echo.*;

import com.codahale.metrics.*;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.unix.DomainSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.HdrHistogram.*;

/**
 * A simple client that requests a greeting from the {@link HelloWorldServer}.
 */
public class EchoClient {
  private static final Logger logger = LoggerFactory.getLogger(EchoClient.class);

  private final ManagedChannel channel;
  private final EchoGrpc.EchoStub asyncStub;

  //private final GreeterGrpc.GreeterBlockingStub blockingStub;

  /**
   * Construct client connecting to HelloWorld server at {@code host:port}.
   */
  public EchoClient(String host, int port) {
//    ManagedChannel channel = NettyChannelBuilder.forAddress(new DomainSocketAddress("/tmp/imageresizer.socket"))
//        .eventLoopGroup(new EpollEventLoopGroup())
//        .channelType(EpollDomainSocketChannel.class)
//        .usePlaintext(true)
//        .build();
    this( ManagedChannelBuilder.forAddress(host, port)
        // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
        // needing certificates.
        .usePlaintext(true)
        .build());
  }

  /**
   * Construct client for accessing RouteGuide server using the existing channel.
   */
  EchoClient(ManagedChannel channel) {
    this.channel = channel;
    asyncStub = EchoGrpc.newStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public class MyThread implements Runnable {

    public MyThread(StreamObserver<MessageRes> str) {
      // store parameter for later user
    }

    public void run() {
    }
  }


  /**
   * Bi-directional example, which can only be asynchronous.
   */
  public CountDownLatch echoTest() {
    logger.info("*** echoTest");
    final CountDownLatch finishLatch = new CountDownLatch(1);
    // A Histogram covering the range from 1 nsec to 1 min with 3 decimal point resolution:
    //final Histogram histogram = new Histogram(60000000000L, 3);
    final MetricRegistry metricRegistry = new MetricRegistry();
    final Timer latency = metricRegistry.timer("latency");
    final Slf4jReporter slf4jReporter = Slf4jReporter.forRegistry(metricRegistry)
        .outputTo(LoggerFactory.getLogger("boilerplate.grpc.metrics"))
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build();
    //slf4jReporter.start(20,TimeUnit.SECONDS);




    final long WARMUP_MESSAGES = 10000;
    long RUN_MESSAGES = 10000;

    StreamObserver<MessageRes> responseObserver = new StreamObserver<MessageRes>() {
      @Override
      public void onNext(MessageRes value) {
        logger.debug("responseObserver onNext {}", value.toString());
        logger.debug("Thread responseObserver - {}", Thread.currentThread().toString());
        long lat = System.nanoTime() - value.getTs();
        logger.debug("Latency: {}",lat);
        if (value.getMessageCount() >= WARMUP_MESSAGES) {
          //logger.info("Reset histogram");
          //histogram.reset();
          latency.update(lat, TimeUnit.NANOSECONDS);
          //histogram.recordValue(lat);

        }
      }

      @Override
      public void onError(Throwable t) {
        logger.error("responseObserver onError {}", t);
        finishLatch.countDown();
      }

      @Override
      public void onCompleted() {
        logger.info("responseObserver onCompleted");
        slf4jReporter.report();
        finishLatch.countDown();
      }
    };

    StreamObserver<MessageReq> requestObserver =
        asyncStub.bidirectionalStream(responseObserver);


    try {
      byte[] b = new byte[20];
      //new Random().nextBytes(b);
      //ByteString.copyFrom(b);
      Arrays.fill(b, (byte) 0xFF);
      logger.info("Start sending messages...");
      for (int i = 0; i < WARMUP_MESSAGES+RUN_MESSAGES; i++) {
        MessageReq request = MessageReq.newBuilder().setTs(System.nanoTime()).setMessageCount(i).setBurstCount(0).setPayload(ByteString.copyFrom(b)).build();
        logger.debug("requestObserver onNext {}", request);
        logger.debug("Thread requestObserver - {}", Thread.currentThread().toString());
        requestObserver.onNext(request);
        Thread.sleep(1);
      }
    } catch (Exception e) {
      // Cancel RPC
      requestObserver.onError(e);
      logger.error("Fatal exception {}",e);
    }
    // Mark the end of requests
    requestObserver.onCompleted();


    // return the latch while receiving happens asynchronously
    return finishLatch;
  }

  /**
   * Greet server. If provided, the first element of {@code args} is the name to use in the
   * greeting.
   */
  public static void main(String[] args) throws Exception {
    Runnable task2 = () -> {
      EchoClient client = new EchoClient("localhost", 50051);
      try {
        // Send and receive some notes.
        CountDownLatch finishLatch = client.echoTest();
        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
          logger.error("client can not finish within 1 minutes");
        }
        client.shutdown();
      } catch (Exception e) {
        logger.error("Exception {}",e);
      }
    };

// start the thread
    new Thread(task2).start();

  }
}
