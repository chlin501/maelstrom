name := "maelstrom-scala-demo"
organization := "lab"
scalaVersion := "2.13.10"

lazy val echo = project
  .settings(
    name := "echo",
    scalaVersion := "2.13.10",
    libraryDependencies ++= Seq(
      "com.eclipsesource.minimal-json" % "minimal-json" % "0.9.5",
      "org.scalatest" %% "scalatest" % "3.2.18" % "test"
    )
  )

lazy val root = project
  .in(file("."))
  .aggregate(echo)
