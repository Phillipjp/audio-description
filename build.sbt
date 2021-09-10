import Dependencies._

scalaVersion     := "2.13.6"
version          := "0.1.0-SNAPSHOT"
organization     := "com.example"
organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "vodudiodescription",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-xml" % "2.0.0",
      scalaTest % Test
      )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
