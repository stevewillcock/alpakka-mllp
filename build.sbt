name := "alpakka-mllp"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "0.14",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)
