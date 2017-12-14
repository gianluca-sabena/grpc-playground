/*

--- This must be a java class ---

- see https://issues.scala-lang.org/browse/SI-7936
- see https://github.com/xuwei-k/grpc-scala-sample/pull/5


 */
package boilerplate.grpc;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class FullServerCallListener<R> extends ForwardingServerCallListener<R> {
  private static final Logger logger = LoggerFactory.getLogger(FullServerCallListener.class);
  private final ServerCall.Listener<R> delegate;
  private final GrpcMethod grpcMethod;
  private final Metadata requestHeaders;

  FullServerCallListener(
      ServerCall.Listener<R> delegate, Metadata requestHeaders, GrpcMethod grpcMethod) {
    this.delegate = delegate;
    this.grpcMethod = grpcMethod;
    this.requestHeaders = requestHeaders;

  }

  @Override
  protected ServerCall.Listener<R> delegate() {
    return delegate;
  }

  private String getHeaderClientId() {
    final Metadata.Key<String> CUSTOM_HEADER_KEY =
        Metadata.Key.of("senderId", Metadata.ASCII_STRING_MARSHALLER);
    return requestHeaders.get(CUSTOM_HEADER_KEY);
  }




//  @Override
//  public void onMessage(R request) {
//    logger.debug(" --- MESSAGE ---");
//    super.onMessage(request);
//  }


  @Override
  public void onHalfClose() {
    logger.debug(" --- onHalfClose --- clientId: {}",getHeaderClientId());
    delegate().onHalfClose();
  }

  @Override
  public void onCancel() {
    logger.debug(" --- onCancel --- clientId: {}",getHeaderClientId());
    delegate().onCancel();
  }

  @Override
  public void onComplete() {
    logger.debug(" --- onComplete --- clientId: {}",getHeaderClientId());
    delegate().onComplete();
  }

  @Override
  public void onReady() {

    //logger.debug(" --- onReady --- grpc info: {} {} {}",grpcMethod.methodName(),grpcMethod.serviceName(),grpcMethod.type());
    logger.debug(" --- onReady --- clientId: {}",getHeaderClientId());
    delegate().onReady();
  }

}
