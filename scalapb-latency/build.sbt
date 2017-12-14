import com.trueaccord.scalapb.compiler.Version.{grpcJavaVersion, scalapbVersion, protobufVersion}

name := "grpc-playground-scala-latency"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions ++= ("-feature" :: Nil)

PB.targets in Compile := Seq(
  PB.gens.java -> (sourceManaged in Compile).value,
  scalapb.gen(javaConversions=true) -> (sourceManaged in Compile).value
)

lazy val akkaVersion = "2.4.14"

libraryDependencies ++= Seq(
  "io.grpc" % "grpc-netty" % com.trueaccord.scalapb.compiler.Version.grpcJavaVersion,
  "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % com.trueaccord.scalapb.compiler.Version.scalapbVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka"     %% "akka-slf4j"     % akkaVersion,
  "com.typesafe"               % "config"                % "1.3.1",
  "com.iheart"                 %% "ficus"                % "1.2.3",
  "ch.qos.logback"             % "logback-classic"       % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging"        % "3.5.0",
  "io.dropwizard.metrics"      % "metrics-core"          % "3.1.2",
  "io.dropwizard.metrics"      % "metrics-jvm"           % "3.1.2"
)

fork in run := true
cancelable in Global := true

scalafmtConfig in ThisBuild := Some(file(".scalafmt.conf"))
formatSbtFiles in ThisBuild := true

assemblyJarName in assembly := "app.jar"

assemblyMergeStrategy in assembly := {
  case "META-INF/MANIFEST.MF"         => MergeStrategy.discard
  case PathList("META-INF", xs @ _ *) => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
