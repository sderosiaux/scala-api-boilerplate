package example

import cats.effect._
import fs2.Stream.Compiler._
import cats.effect.ExitCode
import cats.implicits._
import example.route.{ ApiRoutes, RawHttp4sRoute }
import example.ziomodules.{ RemoteStringServicesModule, StringServicesModule }
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
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
import scala.concurrent.ExecutionContext.global

object HttpApp extends App {
  type AppEnvironment = Clock with StringServicesModule // with UserRepository with MyLogger

  override def run(args: List[String]) = {
    val prog: ZIO[ZEnv, Throwable, Unit] = for {
      conf <- ZIO.effect(ApplicationConf.build().orThrow())
      _ <- putStrLn(conf.toString)
      server: ZIO[AppEnvironment, Throwable, Unit] = runHttp(conf)
      prog <- runClient[ZEnv, Unit] { cc =>
        val x = server.provideSome[ZEnv] { currentEnv =>
          new Clock with RemoteStringServicesModule {
            override val clock: Clock.Service[Any] = currentEnv.clock
            override val client: Client[Task] = cc
          }
        }
        x
      }
    } yield prog

    prog.foldM(h => putStrLn(h.toString).as(1), _ => ZIO.succeed(0))
  }

  private def runClient[R, A](f: Client[Task] => ZIO[R, Throwable, A]): ZIO[R, Throwable, A] =
    ZIO.runtime[R].flatMap { implicit rts =>
      BlazeClientBuilder[RIO[Any, *]](global).resource.toManagedZIO.use(f)
    }

  private def runHttp(conf: ApplicationConf): ZIO[AppEnvironment, Throwable, Unit] = {
    val apiRoutes = new ApiRoutes[AppEnvironment]()
    val openApiRoutes = OpenApi.route[AppEnvironment](apiRoutes.endpoints)

    ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
      val rawRoute = RawHttp4sRoute.buildRaw[RIO[AppEnvironment, *]]()
      val allTapirRoutes = apiRoutes.getRoutes.foldK

      val httpApp: HttpApp[RIO[AppEnvironment, *]] = (rawRoute <+> allTapirRoutes <+> openApiRoutes).orNotFound
      val httpAppExtended = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

      BlazeServerBuilder[RIO[AppEnvironment, *]]
        .bindHttp(conf.port.port.value, conf.server.value)
        .withHttpApp(httpAppExtended)
        .withoutBanner
        .withSocketKeepAlive(true)
        .withTcpNoDelay(true)
        .serve
        .compile[RIO[AppEnvironment, *], RIO[AppEnvironment, *], ExitCode]
        .drain
    }
  }
}
