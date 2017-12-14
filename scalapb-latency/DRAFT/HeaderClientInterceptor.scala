package boilerplate.grpc.util

import java.util.UUID
import java.util.logging.Logger

import io.grpc._

object HeaderClientInterceptor {
  private val logger: Logger = Logger.getLogger(classOf[HeaderClientInterceptor].getName)
  private val customHeadKey: Metadata.Key[String] = Metadata.Key.of(
    "senderId",
    Metadata.ASCII_STRING_MARSHALLER
  )
}

class HeaderClientInterceptor extends ClientInterceptor {
  def interceptCall[ReqT, RespT](
                                  method: MethodDescriptor[ReqT, RespT],
                                  callOptions: CallOptions,
                                  next: Channel
                                ): ClientCall[ReqT, RespT] = {
    new ForwardingClientCall.SimpleForwardingClientCall[ReqT, RespT](next.newCall(method, callOptions)) {



      override def start(responseListener: ClientCall.Listener[RespT], headers: Metadata) = {
        headers.put(HeaderClientInterceptor.customHeadKey, UUID.randomUUID().toString)
        super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener[RespT](responseListener) {
          override def onHeaders(headers: Metadata) = {
            HeaderClientInterceptor.logger.info("header received from server:" + headers)
            super.onHeaders(headers)
          }
        }, headers)
      }
    }
  }
}
