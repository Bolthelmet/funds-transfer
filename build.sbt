name := """money-transfer"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  "com.typesafe.play" %% "anorm" % "2.5.2",
  ws,
  filters,
  evolutions,
  "net.codingwell" %% "scala-guice" % "4.1.0",

  specs2 % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)


//javaOptions in Test += "-Dconfig.file=conf/application-test.conf"

scalacOptions ++= Seq("-feature")
