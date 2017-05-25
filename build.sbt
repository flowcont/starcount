name := "datasystems_interview"
scalaVersion in ThisBuild := "2.12.2"

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "com.sksamuel.avro4s" % "avro4s-core_2.12" % "1.6.4"
  )
)

lazy val common = project
  .settings(commonSettings)

