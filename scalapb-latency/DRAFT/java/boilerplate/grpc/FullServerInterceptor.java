/*

--- This must be a java class ---

- see https://issues.scala-lang.org/browse/SI-7936
- see https://github.com/xuwei-k/grpc-scala-sample/pull/5


 */
package boilerplate.grpc;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;


public class FullServerInterceptor implements ServerInterceptor {

  @Override
  public <R, S> ServerCall.Listener<R> interceptCall(
      ServerCall<R, S> call,
      Metadata requestHeaders,
      ServerCallHandler<R, S> next) {

    MethodDescriptor<R, S> method = call.getMethodDescriptor();


    GrpcMethod grpcMethod = GrpcMethod.of(method);
    ServerCall<R, S> serverCall = new FullServerCall(call, grpcMethod);

    return new FullServerCallListener<>(
        next.startCall(serverCall, requestHeaders), requestHeaders, GrpcMethod.of(method));
  }

}