# Some experiments with grpc 

See Google grpc http://www.grpc.io/

* hello-world -> basci hello world example ported from google grpc example https://github.com/grpc/grpc-java/tree/master/examples
* reactive -> bind grpc streams to rxScala observable https://github.com/ReactiveX/RxScala


## Requirements

The simple way is to follow the gRPC demo instructions https://github.com/grpc/grpc-common/tree/master/java

 * Maven 3.2 https://maven.apache.org/download.cgi (on Mac OS X maven 3.2 is not present in brew, just download it and
symlink the binary apache-maven-3.2.5/bin/mvn in /usr/bin/mvn)
 * Google protobuffer 3 https://github.com/google/protobuf/tree/v3.0.0-alpha-2 (see note for Mac user at the end)
 * Google gRPC Java in order to have Netty 4 and gRPC java service compiler https://github.com/grpc/grpc-java#how-to-build

## Settings

Update build.sbt with java gRPC protobuf plugin path. It create a full transport service based on .proto file description.
