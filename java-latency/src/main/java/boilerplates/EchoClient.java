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

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
    this(ManagedChannelBuilder.forAddress(host, port)
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
    // A Histogram covering the range from 1 nsec to 1 hour with 3 decimal point resolution:
    final Histogram histogram = new Histogram(5);
    final long WARMUP_MESSAGES = 10;
    long RUN_MESSAGES = 100;

    StreamObserver<MessageRes> responseObserver = new StreamObserver<MessageRes>() {
      @Override
      public void onNext(MessageRes value) {
        logger.debug("responseObserver onNext {}", value.toString());
        logger.debug("Thread responseObserver - {}", Thread.currentThread().toString());
        long lat = System.nanoTime() - value.getTs();
        logger.debug("Latency: {}",lat);
        if (value.getMessageCount() == WARMUP_MESSAGES) {
          logger.info("Reset histogram");
          histogram.reset();
        }
        histogram.recordValue(lat);
      }

      @Override
      public void onError(Throwable t) {
        logger.debug("responseObserver onError {}", t);
        finishLatch.countDown();
      }

      @Override
      public void onCompleted() {
        logger.debug("responseObserver onCompleted");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        histogram.outputPercentileDistribution(ps,1000.0);
        logger.info("Percentile: {}",baos.toString());
        ps.close();
        finishLatch.countDown();
      }
    };

    StreamObserver<MessageReq> requestObserver =
        asyncStub.bidirectionalStream(responseObserver);


    try {
      byte[] b = new byte[20];
      new Random().nextBytes(b);
      ByteString.copyFrom(b);
      //Arrays.fill(b, (byte) 0xFF);
      logger.info("Start sending messages...");
      for (int i = 0; i < WARMUP_MESSAGES+RUN_MESSAGES; i++) {
        MessageReq request = MessageReq.newBuilder().setTs(System.nanoTime()).setMessageCount(i).setBurstCount(0).setPayload(ByteString.EMPTY).build();
        logger.debug("requestObserver onNext {}", request);
        logger.debug("Thread requestObserver - {}", Thread.currentThread().toString());
        requestObserver.onNext(request);
      }
    } catch (RuntimeException e) {
      // Cancel RPC
      requestObserver.onError(e);
      throw e;
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
    EchoClient client = new EchoClient("localhost", 50051);
    try {
      // Send and receive some notes.
      CountDownLatch finishLatch = client.echoTest();
      if (!finishLatch.await(1, TimeUnit.MINUTES)) {
        logger.error("client can not finish within 1 minutes");
      }
    } finally {
      client.shutdown();
    }
  }
}
