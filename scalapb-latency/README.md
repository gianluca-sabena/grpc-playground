# Grpc latency

Grpc client and server to measure round trip latency

- To compile the file `hello.proto` run `sbt compile`
- Start receiver `sbt run receiver` to run all the main
- Start sender `sbt run sender` to run all the main

## Notes

Grpc allows to intercept request on server and client
This feature allow, for example, to inject a user Id

Best approach is to use bidirectional stream [More info](https://groups.google.com/d/msg/grpc-io/9My7isdNKn4/1YknGNGoCwAJ)

Google performances [benchmarks](https://performance-dot-grpc-testing.appspot.com/explore?dashboard=5712453606309888)

Other links:

- handle client reconnect <https://groups.google.com/forum/#!topic/grpc-io/-sbIwInGlqI>
- detect client disconnect <https://groups.google.com/d/msg/grpc-io/9My7isdNKn4/1YknGNGoCwAJ>
- Context <https://groups.google.com/d/msg/grpc-io/oO2NtpIzRbM/_TJPYy-aEwAJ>
- issue <https://github.com/grpc/grpc-java/issues/339#issuecomment-262104469>
