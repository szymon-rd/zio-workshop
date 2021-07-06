import Dependencies._

val ZIOVersion        = "1.0.8"
val ZIOLoggingVersion = "0.5.8"
val ZIOQueryVersion   = "0.2.6"
val CalibanVersion = "0.10.1"

ThisBuild / scalaVersion     := "2.13.5"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "zioworkshop"
ThisBuild / organizationName := "zioworkshop"

lazy val root = (project in file("."))
  .settings(
    name := "zio-workshop",
    libraryDependencies += scalaTest % Test
  )


libraryDependencies ++= Seq(
  "dev.zio"       %% "zio"               % ZIOVersion,
  "dev.zio"       %% "zio-logging"       % ZIOLoggingVersion,
  "dev.zio"       %% "zio-logging-slf4j" % ZIOLoggingVersion,
  "dev.zio"       %% "zio-interop-cats"  % "2.3.1.0",
  "dev.zio"       %% "zio-test"          % ZIOVersion % "test",
  "dev.zio"       %% "zio-test-sbt"      % ZIOVersion % "test",
  "com.github.ghostdogpr" %% "caliban"      % CalibanVersion,
  "com.github.ghostdogpr" %% "caliban-play" % CalibanVersion,
  "com.github.ghostdogpr" %% "caliban-tools" % CalibanVersion,
)

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
