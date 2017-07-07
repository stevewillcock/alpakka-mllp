name := "alpakka-mllp"

version := "0.1"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-file" % "0.10",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.1",
  "ch.qos.logback" % "logback-classic" % "1.2.2"
)