lazy val root = (project in file(".")).
  settings(
    name := "actor-macro",
    version := "0.0.1",
    scalaVersion := "2.11.6"
  )

libraryDependencies ++= Seq(
  "org.scalaz"                  %%  "scalaz-core"       % "7.1.3",
  "joda-time"                   %   "joda-time"         % "2.8.2",
  "com.typesafe.akka"           %%  "akka-actor"        % "2.4.1",
  "org.scala-lang"              %   "scala-compiler"    % "2.11.7"
)