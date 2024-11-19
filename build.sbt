import sbt.enablePlugins

val scala2Version = "2.13.12"

lazy val root = project
  .in(file("."))
  .settings(
    name := "LLM-hw3",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala2Version,

    scalaVersion := scala2Version,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.6.20",
      "com.typesafe.akka" %% "akka-stream" % "2.6.20",
      "com.typesafe.akka" %% "akka-http" % "10.2.10",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.10", // JSON marshalling/unmarshalling
      "org.scalatest" %% "scalatest" % "3.2.16" % Test,
      "org.scalameta" %% "munit" % "1.0.0" % Test
    ),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) =>
        xs match {
          case "MANIFEST.MF" :: Nil =>   MergeStrategy.discard
          case "services" ::_       =>   MergeStrategy.concat
          case _                    =>   MergeStrategy.discard
        }
      case "reference.conf"  => MergeStrategy.concat
      case x if x.endsWith(".proto") => MergeStrategy.rename
      case x if x.contains("hadoop") => MergeStrategy.first
      case  _ => MergeStrategy.first
    }
  )


resolvers += "Conjars Repo" at "https://conjars.org/repo"
