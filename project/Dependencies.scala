import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "[3.0.3,)"
  lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % "[1.13.5,)"
  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "[3.7.2,)"
  lazy val scConfigDsl = "org.hbel" %% "scconfigdsl" % "[0.1.1,)"
  lazy val minio = "io.minio" % "minio" % "[3.0.9,)"
}
