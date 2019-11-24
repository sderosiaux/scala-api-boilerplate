import Dependencies._

val gitOwner = "sderosiaux"
val gitRepo = "repo"

scalaVersion     := "2.13.1"
version          := "0.1.0"
organization     := "com.example"
organizationName := "example"
startYear        := Some(2019)
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
homepage := Some(url(s"https://github.com/$gitOwner/$gitRepo"))
scmInfo  := Some(ScmInfo(url(s"https://github.com/$gitOwner/$gitRepo"), s"git@github.com:$gitOwner/$gitRepo.git"))
developers += Developer(
  "sderosiaux",
  "Stéphane Derosiaux",
  s"stephane@ixonad.com",
  url(s"https://sderosiaux.com")
)

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.url("typesafe", url("https://repo.typesafe.com/typesafe/ivy-releases/"))(
  Resolver.ivyStylePatterns
)

scalafmtOnCompile         := true
fork in Test              := true
parallelExecution in Test := true

lazy val root = (project in file("."))
  .settings(
    name := "App",
    libraryDependencies ++= Seq(
      scalaTest                       % Test,
      "dev.zio" %% "zio"              % ZioVersion,
      "dev.zio" %% "zio-interop-cats" % ZioCatsVersion,
      "dev.zio" %% "zio-macros-core"  % "0.5.0",
      //"dev.zio" %% "zio-logging"            % ZioVersion,
      "org.http4s" %% "http4s-core"         % Http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe"        % Http4sVersion,
      "org.http4s" %% "http4s-dsl"          % Http4sVersion,
      "io.circe" %% "circe-generic"         % CirceVersion,
      "ch.qos.logback"                      % "logback-classic" % LogbackVersion,
      "is.cir" %% "ciris-cats"              % CirisVersion,
      "is.cir" %% "ciris-cats-effect"       % CirisVersion,
      "is.cir" %% "ciris-core"              % CirisVersion,
      //"is.cir" %% "ciris-enumeratum" % CirisVersion,
      "is.cir" %% "ciris-generic"                            % CirisVersion,
      "is.cir" %% "ciris-refined"                            % CirisVersion,
      "com.softwaremill.tapir" %% "tapir-core"               % TapirVersion,
      "com.softwaremill.tapir" %% "tapir-http4s-server"      % TapirVersion,
      "com.softwaremill.tapir" %% "tapir-swagger-ui-http4s"  % TapirVersion,
      "com.softwaremill.tapir" %% "tapir-openapi-docs"       % TapirVersion,
      "com.softwaremill.tapir" %% "tapir-openapi-circe-yaml" % TapirVersion,
      "com.softwaremill.tapir" %% "tapir-json-circe"         % TapirVersion,
      "org.webjars"                                          % "swagger-ui" % "3.24.3"
    )
  )

scalacOptions --= Seq("-Xfatal-warnings")
scalacOptions += "-Ymacro-annotations"

Compile / run / mainClass := Some("example.HttpApp")
watchTriggeredMessage     := Watch.clearScreenOnTrigger
testOptions in Test += Tests.Argument("-oDF")

addCompilerPlugin(("org.typelevel" %% "kind-projector" % "0.11.0").cross(CrossVersion.full))
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0")

//addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("chk", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
