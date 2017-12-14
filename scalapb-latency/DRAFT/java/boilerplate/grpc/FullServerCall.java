/*

--- This must be a java class ---

- see https://issues.scala-lang.org/browse/SI-7936
- see https://github.com/xuwei-k/grpc-scala-sample/pull/5


 */
package boilerplate.grpc;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class FullServerCall<R,S> extends ForwardingServerCall.SimpleForwardingServerCall<R,S> {



  private static final Logger logger = LoggerFactory.getLogger(FullServerCall.class);


  private final GrpcMethod grpcMethod;


  FullServerCall(
      ServerCall<R,S> delegate,
      GrpcMethod grpcMethod
  ) {
    super(delegate);
    this.grpcMethod = grpcMethod;
  }


//  @Override
//  public void close(Status status, Metadata responseHeaders) {
//    logger.debug("--- CLOSE ---");
//    super.close(status, responseHeaders);
//
//  }
//
//  @Override
//  public void sendMessage(S message) {
//    logger.debug("--- MESSAGE --- grpcMethod: "+grpcMethod.methodName());
//    super.sendMessage(message);
//  }

}