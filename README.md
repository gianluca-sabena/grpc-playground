A first simple example on how to use Goggle gRPC in scala

# How to run

First we need some dependences, the simple way is to follow the gRPC demo instructions https://github.com/grpc/grpc-common/tree/master/java

Requirements:

 * Maven 3.2 https://maven.apache.org/download.cgi (on Mac OS X maven 3.2 is not present in brew, just download it and
symlink the binary apache-maven-3.2.5/bin/mvn in /usr/bin/mvn)
 * Google protobuffer 3 https://github.com/google/protobuf/tree/v3.0.0-alpha-2 (see note for Mac user at the end)
 * Google gRPC Java in order to have Netty 4 and gRPC java service compiler https://github.com/grpc/grpc-java#how-to-build

# Settings:

Update build.sbt with java gRPC protobuf plugin path. It create a full transport service based on .proto
file description.

