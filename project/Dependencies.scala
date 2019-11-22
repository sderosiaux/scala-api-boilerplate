import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"

  val ZioVersion = "1.0.0-RC17"
  val ZioCatsVersion = "2.0.0.0-RC8"
  val Http4sVersion = "0.21.0-M5"
  val LogbackVersion = "1.2.3"
  val CirceVersion = "0.12.3"
  val CirisVersion = "0.13.0-RC1"
  val TapirVersion = "0.11.9"
}
