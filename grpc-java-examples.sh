#!/bin/bash

if [ ! -d "git-clone/grpc-java" ]; then
  mkdir -p git-clone
  cd git-clone
  git clone https://github.com/grpc/grpc-java.git
  cd grpc-java
  git checkout v1.8.0
fi

cd grpc-java/examples
mvn verify