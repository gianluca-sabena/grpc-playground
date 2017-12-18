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
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.logging.Logger;

import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
public class EchoServer {
  private static final Logger logger = Logger.getLogger(EchoServer.class.getName());

  private Server server;

  private void start() throws IOException {
    /* The port on which the server should run */
    int port = 50051;
    server = ServerBuilder.forPort(port)
        .addService(new EchoImpl())
        .build()
        .start();
    logger.info("Server started, listening on " + port);

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown hook.
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        EchoServer.this.stop();
        System.err.println("*** server shut down");
      }
    });
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  /**
   * Main launches the server from the command line.
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    final EchoServer server = new EchoServer();
    server.start();
    server.blockUntilShutdown();
  }

  static class EchoImpl extends EchoGrpc.EchoImplBase {


    @Override
    public StreamObserver<MessageReq> bidirectionalStream(
        final StreamObserver<MessageRes> responseObserver) {

      return new StreamObserver<MessageReq>() {
        @Override
        public void onCompleted() {
          logger.info("Got onCompleted");
          responseObserver.onCompleted();
        }

        @Override
        public void onError(Throwable t) {
          logger.info("Got onError - Exception: "+t.toString());
        }

        @Override
        public void onNext(MessageReq req) {
          logger.info("Got a message with ts:" +req.getTs() );
          MessageRes res = MessageRes.newBuilder()
              .setTs(req.getTs())
              .setMessageCount(req.getMessageCount())
              .setBurstCount(req.getMessageCount())
              .build();
          responseObserver.onNext(res);
        }
      };

    }
  }

}
