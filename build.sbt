
name := "datasystems_interview"

val slf4jVersion = "1.7.25"
val akkaVersion = "2.5.2"

lazy val commonSettings = Seq(
  scalaVersion := "2.12.2",
  libraryDependencies ++= Seq(
    "com.sksamuel.avro4s" %% "avro4s-core" % "1.6.4",
    "org.scalacheck" %% "scalacheck" % "1.13.5" % "it,test",
    "org.scalatest" %% "scalatest" % "3.0.1" % "it,test",
    "org.slf4j" % "slf4j-api" % slf4jVersion,
    "org.slf4j" % "slf4j-log4j12" % slf4jVersion,
    "commons-io" % "commons-io" % "2.5"
  )
)
lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    Defaults.itSettings
  )

lazy val common = project
  .configs(IntegrationTest)
  .settings(commonSettings, Defaults.itSettings)

lazy val akkaSettings = Seq(
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
  )
)

lazy val akka = project
  .configs(IntegrationTest)
  .settings(commonSettings ++ akkaSettings, Defaults.itSettings)
  .dependsOn(common)