package boilerplates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 */
public class App {
  private static Logger LOGGER = LoggerFactory.getLogger(EchoClient.class);
  public static void main(String[] args) {
    LOGGER.debug("Hello World!");
    System.out.println("Hello World!");
  }
}
