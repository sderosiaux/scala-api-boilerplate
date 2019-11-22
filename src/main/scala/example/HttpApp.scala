package example

import cats.effect.ExitCode
import cats.implicits._
import example.route.{ ApiRoutes, RawHttp4sRoute }
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.{ HttpApp, HttpRoutes }
import tapir.Endpoint
import tapir.docs.openapi._
import tapir.openapi.circe.yaml._
import tapir.openapi.{ Contact, Info }
import tapir.swagger.http4s.SwaggerHttp4s
import zio.clock.Clock
import zio.console._
import zio.interop.catz._
import zio.{ App, RIO, ZIO, _ }

object HttpApp extends App {
  type AppEnvironment = Clock // with UserRepository with MyLogger

  def makeOpenApiRoute[R](endpoints: List[Endpoint[_, _, _, _]]): HttpRoutes[RIO[R, *]] = {
    val openApi = endpoints.toOpenAPI(
      Info("My App", "1.0.0", "My great App".some, contact = Contact("@sderosiaux".some, email = None, url = None).some)
    )
    new SwaggerHttp4s(openApi.toYaml, "", "openapi.yaml").routes[RIO[R, *]]
  }

  override def run(args: List[String]) = {
    val prog: ZIO[ZEnv, Throwable, Unit] = for {
      conf <- ZIO.effect(ApplicationConf.build().orThrow())
      _ <- putStrLn(conf.toString)
      server = ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
        val apiRoutes = new ApiRoutes[AppEnvironment]()
        val openApi = makeOpenApiRoute[AppEnvironment](apiRoutes.endpoints)
        val rawRoute = RawHttp4sRoute.buildRaw[RIO[AppEnvironment, *]]()
        val allTapirRoutes = apiRoutes.getRoutes.foldK

        val httpApp: HttpApp[RIO[AppEnvironment, *]] = (rawRoute <+> allTapirRoutes <+> openApi).orNotFound
        val httpAppExtended = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

        BlazeServerBuilder[ZIO[AppEnvironment, Throwable, *]]
          .bindHttp(conf.port.port.value, conf.server.value)
          .withHttpApp(httpAppExtended)
          .withoutBanner
          .withSocketKeepAlive(true)
          .withTcpNoDelay(true)
          .serve
          .compile[RIO[AppEnvironment, *], RIO[AppEnvironment, *], ExitCode]
          .drain
      }
      prog <- server.provideSome[ZEnv] { currentEnv =>
        new Clock {
          override val clock: Clock.Service[Any] = currentEnv.clock
        }
      }
    } yield prog

    prog.foldM(h => putStrLn(h.toString).as(1), _ => ZIO.succeed(0))
  }

  //  private val userRoute = new UserRoute[AppEnvironment]
  //  private val yaml = userRoute.getEndPoints.toOpenAPI("User", "1.0").toYaml
  //  private val httpApp =
  //    Router("/" -> userRoute.getRoutes, "/docs" -> new SwaggerHttp4s(yaml).routes[RIO[AppEnvironment, *]]).orNotFound
  //  private val finalHttpApp = Logger.httpApp[ZIO[AppEnvironment, Throwable, *]](true, true)(httpApp)
  //
  //  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
  //    val result = for {
  //      applicationConfig <- ZIO.fromTry(Try(Application.getConfig))
  //      server = ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
  //        BlazeServerBuilder[ZIO[AppEnvironment, Throwable, *]]
  //          .bindHttp(applicationConfig.server.port, applicationConfig.server.host.getHostAddress)
  //          .withHttpApp(finalHttpApp)
  //          .serve
  //          .compile[ZIO[AppEnvironment, Throwable, *], ZIO[AppEnvironment, Throwable, *], ExitCode]
  //          .drain
  //      }
  //      program <- server.provideSome[ZEnv] { base =>
  //        new Clock with LiveUserRepository with LiveLogger {
  //          val clock: Clock.Service[Any] = base.clock
  //          val config: Config = ConfigFactory.parseMap(
  //            Map(
  //              "dataSourceClassName" -> applicationConfig.database.className.value,
  //              "dataSource.url" -> applicationConfig.database.url.value,
  //              "dataSource.user" -> applicationConfig.database.user.value,
  //              "dataSource.password" -> applicationConfig.database.password.value
  //            ).asJava)
  //        }
  //      }
  //    } yield program
  //
  //    result
  //      .foldM(failure = err => putStrLn(s"Execution failed with: \$err") *> ZIO.succeed(1), success = _ => ZIO.succeed(0))
  //  }
}
