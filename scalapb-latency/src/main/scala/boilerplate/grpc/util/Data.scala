package boilerplate.grpc.util

object Data {
  case class Endpoint(label:String, payload:Int, requests:Int,interval:Int)
}
