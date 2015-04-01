import sbtprotobuf.{ProtobufPlugin=>PB}

Seq(PB.protobufSettings: _*)

name := """grpc-playground"""

organization  := "gianluca-sabena"

version       := "0.0.1"

scalaVersion  := "2.11.4"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

//
//  Update this value according to your configuration
//

val grpcJavaProtobufPlugin = Path.userHome.absolutePath+"/repos/clones/grpc-java/compiler/build/binaries/java_pluginExecutable/java_plugin"

libraryDependencies ++= Seq(
  "io.grpc" % "grpc-okhttp" % "0.1.0-SNAPSHOT",
  "io.grpc" % "grpc-core" % "0.1.0-SNAPSHOT",
  "io.grpc" % "grpc-netty" % "0.1.0-SNAPSHOT",
  "io.grpc" % "grpc-stub" % "0.1.0-SNAPSHOT"
)

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
)

version in PB.protobufConfig := "3.0.0-alpha-2"

protoc in PB.protobufConfig := "/usr/local/bin/protoc"

// Need a grpc plugin to generate grpc service class from .proto file

protocOptions in PB.protobufConfig ++= Seq(
  "--plugin=protoc-gen-java_rpc="+grpcJavaProtobufPlugin,
  "--java_rpc_out=target/scala-2.11/src_managed/main/compiled_protobuf"
)