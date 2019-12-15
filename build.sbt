import Dependencies._
import sbt.Keys.{ resolvers, scalacOptions }
import sbt.Test

val gitOwner = "sderosiaux"
val gitRepo = "repo"

lazy val commonSettings = Seq(
  scalaVersion     := "2.13.1",
  version          := "0.1.0",
  organization     := "com.example",
  organizationName := "example",
  startYear        := Some(2019),
  licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
  homepage := Some(url(s"https://github.com/$gitOwner/$gitRepo")),
  scmInfo  := Some(ScmInfo(url(s"https://github.com/$gitOwner/$gitRepo"), s"git@github.com:$gitOwner/$gitRepo.git")),
  developers += Developer(
    "sderosiaux",
    "Stéphane Derosiaux",
    s"stephane@ixonad.com",
    url(s"https://sderosiaux.com")
  ),
  scalafmtOnCompile        := true,
  Test / fork              := true,
  Test / parallelExecution := true,
  sources in (Compile, doc) := Seq.empty, // no java|scaladoc
  publishArtifact in (Compile, packageDoc) := false,
  resolvers += Resolver.sonatypeRepo("releases"),
  resolvers += Resolver.sonatypeRepo("snapshots"),
  resolvers += Resolver.url("typesafe", url("https://repo.typesafe.com/typesafe/ivy-releases/"))(
    Resolver.ivyStylePatterns
  ),
  Compile / compile / wartremoverErrors ++= Warts.all.filterNot(
    Set(
      Wart.ExplicitImplicitTypes, // issues with @newtype
      Wart.ImplicitConversion, // issues with @newtype
      Wart.ImplicitParameter, // issues with @newtype
      Wart.PublicInference, // issues with @newtype
      Wart.Nothing, // ZIO
//    Wart.Overloading,
      Wart.Any, // ZIO
//    Wart.Equals, // keep, easier that way
      Wart.ToString, // keep, easier that way
//    Wart.Product, // keep, false positives all around
      Wart.JavaSerializable, // ZIO
      Wart.Serializable // ZIO
//    Wart.DefaultArguments // for constructors for PureConfig
    )
  ),
  scalacOptions --= Seq("-Xfatal-warnings"),
  scalacOptions ++= Seq("-Ymacro-annotations") // , "-Vmacro-lite")
)

lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "Scala API Boilerplate",
    // publish / skip     := true // when fully modularized
    libraryDependencies ++= Seq(
      scalaTest                       % Test,
      "dev.zio" %% "zio"              % ZioVersion,
      "dev.zio" %% "zio-interop-cats" % ZioCatsVersion,
      "dev.zio" %% "zio-test"         % ZioVersion % Test,
      "dev.zio" %% "zio-test-sbt"     % ZioVersion % Test,
      //"dev.zio" %% "zio-macros-core"  % "0.5.0", // IntelliJ can't find the generated code -_-
      //"dev.zio" %% "zio-logging"            % ZioVersion, // Not published yet
      "org.http4s" %% "http4s-core"                          % Http4sVersion,
      "org.http4s" %% "http4s-blaze-server"                  % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client"                  % Http4sVersion,
      "org.http4s" %% "http4s-circe"                         % Http4sVersion,
      "org.http4s" %% "http4s-dsl"                           % Http4sVersion,
      "io.circe" %% "circe-generic"                          % CirceVersion % Test,
      "io.circe" %% "circe-literal"                          % CirceVersion,
      "ch.qos.logback"                                       % "logback-classic" % LogbackVersion,
      "com.github.pureconfig" %% "pureconfig"                % "0.12.1",
      "eu.timepit" %% "refined-pureconfig"                   % "0.9.10",
      "com.softwaremill.tapir" %% "tapir-core"               % TapirVersion,
      "com.softwaremill.tapir" %% "tapir-http4s-server"      % TapirVersion,
      "com.softwaremill.tapir" %% "tapir-swagger-ui-http4s"  % TapirVersion,
      "com.softwaremill.tapir" %% "tapir-openapi-docs"       % TapirVersion,
      "com.softwaremill.tapir" %% "tapir-openapi-circe-yaml" % TapirVersion,
      "com.softwaremill.tapir" %% "tapir-json-circe"         % TapirVersion,
      "org.webjars"                                          % "swagger-ui" % "3.24.3"
    )
  )
  .aggregate(model)
  .dependsOn(model)

lazy val model = project
  .in(file("modules/model"))
  .settings(commonSettings)
  .settings(
    name        := "Models",
    description := "The domain classes of the project",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.estatico" %% "newtype"    % "0.4.3",
      "io.circe" %% "circe-literal" % CirceVersion
    )
  )

Compile / run / mainClass := Some("example.ComplexApp")

ThisBuild / useSuperShell         := false
ThisBuild / turbo                 := true
ThisBuild / watchTriggeredMessage := Watch.clearScreenOnTrigger
ThisBuild / testOptions in Test += Tests.Argument("-oDF")
ThisBuild / testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

addCompilerPlugin(("org.typelevel" %% "kind-projector" % "0.11.0").cross(CrossVersion.full))
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0")

//addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("chk", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
