name := """NotificationSender"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.12"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "6.0.0-RC2" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
dependencyOverrides ++= Seq(
  "com.google.inject" % "guice" % "5.1.0",
  "com.google.inject.extensions" % "guice-assistedinject" % "5.1.0"
)


// database
libraryDependencies ++= Seq(
  guice,
  jdbc,
  "mysql" % "mysql-connector-java" % "8.0.27",
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0" % Test
)

//akka
val akkaVersion = "2.6.11"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
//  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
//  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion
)

//mailer
val mailerVersion = "8.0.1"
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-mailer" % mailerVersion,
  "com.typesafe.play" %% "play-mailer-guice" % mailerVersion
)

//kafka
val kafkaVersion = "2.6.0"
libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % kafkaVersion
)
