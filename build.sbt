val scalaVersion_2_13 = "2.13.8"

lazy val root = project
  .in(file("."))
  .settings(
    name := "gigahorse-scalaj-http",
    organization := "com.github.tototoshi",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scalaVersion_2_13,
    libraryDependencies ++= Seq(
      "com.eed3si9n" %% "gigahorse-core" % "0.6.0",
      "com.eed3si9n" %% "gigahorse-okhttp" % "0.6.0",
      "com.eed3si9n" %% "gigahorse-akka-http" % "0.6.0",
      "org.scalaj" %% "scalaj-http" % "2.4.2",
      "org.slf4j" % "slf4j-api" % "1.7.36",
      "ch.qos.logback" % "logback-classic" % "1.2.11",
      "org.scalatest" %% "scalatest" % "3.2.12" % "test"
    )
  )
