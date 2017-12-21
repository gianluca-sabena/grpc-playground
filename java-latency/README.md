# GRPC Java examples

## Hello world example

Same code as <https://github.com/grpc/grpc-java/tree/master/examples/src/main/java/io/grpc/examples/helloworld>

Open two terminal

- Start Server `mvn compile exec:java -Dexec.mainClass=boilerplates.HelloWorldServer`
- Start Client `mvn compile exec:java -Dexec.mainClass=boilerplates.HelloWorldClient`

## Echo server

Run with maven

- Start Server `mvn compile exec:java -Dexec.mainClass=boilerplates.EchoServer`
- Start Client `mvn compile exec:java -Dexec.mainClass=boilerplates.EchoClient`

Run uber jar

- Start Server `java -cp target/java-latency-1.8.0.jar boilerplates.EchoServer`
- Start Client `java -cp target/java-latency-1.8.0.jar boilerplates.EchoClient`