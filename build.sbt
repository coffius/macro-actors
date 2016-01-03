lazy val root = (project in file(".")).
  settings(
    name := "actor-macro",
    version := "0.0.1",
    scalaVersion := "2.11.7"
  )

libraryDependencies ++= Seq(
  "com.typesafe.akka"           %%  "akka-actor"                  % "2.4.1",
  "com.typesafe.akka"           %%  "akka-testkit"                % "2.4.1",
  "org.scala-lang"              %   "scala-compiler"              % "2.11.7",
  "org.scalamock"               %% "scalamock-scalatest-support"  % "3.2"     % "test",
  "org.scalatest"               %% "scalatest"                    % "2.2.4"   % "test"
)