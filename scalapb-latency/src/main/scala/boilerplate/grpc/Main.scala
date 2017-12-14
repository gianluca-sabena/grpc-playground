package boilerplate.grpc

import boilerplate.grpc.receiver._
import boilerplate.grpc.sender._
import boilerplate.grpc.util.Settings
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger

object Main {
  val logger = Settings.logger
  val conf   = ConfigFactory.load()

  def main(args: Array[String]): Unit = {

    if (args.length > 0 && args(0) == "receiver") ReceiverSimple.start()
    else if (args.length > 0 && args(0) == "sender") SenderLatency.run()
    else logger.error(s"Error! Usage: command receiver | sender   ")
  }
  // Catch a Ctrl+C = sig term OR
  // In a shell run `# kill -TERM $pid`
  // mesos termination https://github.com/mesosphere/marathon/issues/4323
  scala.sys.addShutdownHook {
    logger.info("ShutdownHook Stopping...")
    println("ShutdownHook Stopping...")
    ReceiverSimple.stop()
    SenderLatency.stop()
    println("ShutdownHook Stopped...")
    logger.info("ShutdownHook Stopped...")
    Thread.sleep(5000)
  }
}
