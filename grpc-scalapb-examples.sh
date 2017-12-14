#!/bin/bash

if [ ! -d "git-clone/grpc-scala-sample" ]; then
  mkdir -p git-clone
  cd git-clone
  git clone https://github.com/xuwei-k/grpc-scala-sample.git
  cd grpc-scala-sample
  git submodule update --init --recursive
  cd ../..
fi

cd git-clone/grpc-scala-sample

echo " Run sbt; project grpcScalaSample; compile"

sbt 'project grpcScalaSample' compile