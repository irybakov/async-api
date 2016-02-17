organization in ThisBuild := "kz.rio"

name := "async-api"

version := "1.0"

scalaVersion := "2.11.7"

scalacOptions := Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += "spray repo" at "http://repo.spray.io"

val sprayVersion = "1.3.3"
val akkaVersion = "2.4.1"
val json4sVersion = "3.2.11"
val aerospike = "3.1.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "io.spray" %% "spray-can" % sprayVersion,
  "io.spray" %% "spray-routing" % sprayVersion,
  "org.json4s" %% "json4s-native" % json4sVersion,
  "com.rabbitmq"      %   "amqp-client"       % "3.5.3",
  "com.github.sstone" %   "amqp-client_2.11"  % "1.5"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "io.spray" %% "spray-testkit" % sprayVersion % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)

enablePlugins(JavaAppPackaging)

Revolver.settings
    