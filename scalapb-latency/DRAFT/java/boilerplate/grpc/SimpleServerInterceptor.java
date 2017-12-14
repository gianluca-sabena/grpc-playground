/*

--- This must be a java class ---

- see https://issues.scala-lang.org/browse/SI-7936
- see https://github.com/xuwei-k/grpc-scala-sample/pull/5


 */
package boilerplate.grpc;

import io.grpc.*;
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A interceptor to handle server header.
 */
public class SimpleServerInterceptor implements ServerInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(SimpleServerInterceptor.class.getName());

  static final Metadata.Key<String> CUSTOM_HEADER_KEY =
      Metadata.Key.of("exampleHeader", Metadata.ASCII_STRING_MARSHALLER);


  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call,
      final Metadata requestHeaders,
      ServerCallHandler<ReqT, RespT> next) {


    logger.info("header received from client:" + requestHeaders);

    return next.startCall(new SimpleForwardingServerCall<ReqT, RespT>(call) {

      @Override
      public void close(Status status, Metadata responseHeaders) {
        logger.debug("--- CLOSE ---");
        super.close(status, responseHeaders);
      }


      @Override
      public void sendHeaders(Metadata responseHeaders) {
        responseHeaders.put(CUSTOM_HEADER_KEY, "example value");
        super.sendHeaders(responseHeaders);
      }
    }, requestHeaders);
  }
}