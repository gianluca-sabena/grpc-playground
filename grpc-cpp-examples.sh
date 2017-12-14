#!/bin/bash

if [ ! -d "git-clone/grpc" ]; then
  mkdir -p git-clone
  cd git-clone
  git clone -b $(curl -L https://grpc.io/release) https://github.com/grpc/grpc
fi

cd grpc/examples/cpp/helloworld
make 