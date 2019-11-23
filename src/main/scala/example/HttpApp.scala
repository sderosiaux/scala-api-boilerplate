package example

import cats.effect.ExitCode
import cats.implicits._
import example.route.{ ApiRoutes, ExternalApiRoutes, RawHttp4sRoute }
import example.ziomodules.{ RemoteStringServicesModule, StringServicesModule }
import fs2.Stream.Compiler._
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import zio.clock.Clock
import zio.console._
import zio.interop.catz._
import zio.{ App, RIO, ZIO, _ }
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

object HttpApp extends App {
  type AppEnvironment = Clock with StringServicesModule // with UserRepository with MyLogger

  override def run(args: List[String]) =
    (for {
      conf <- ZIO.effect(ApplicationConf.build().orThrow())
      _ <- putStrLn(conf.toString)
      server: ZIO[AppEnvironment, Throwable, Unit] = runHttp(conf)
      blockingEC <- blocking.blockingExecutor.map(_.asEC)
      //client <- makeClient(blockingEC)
      prog <- runClient[ZEnv, Unit] { cc =>
        val x = server.provideSome[ZEnv] { currentEnv =>
          new Clock with RemoteStringServicesModule {
            override val clock: Clock.Service[Any] = currentEnv.clock
            override val client: Client[Task] =
              org.http4s.client.middleware.Logger(logHeaders = true, logBody = false)(cc)
          }
        }
        x
      }
    } yield prog).foldM(h => putStrLn(h.toString).as(1), _ => ZIO.succeed(0))

  private def runClient[R, A](f: Client[Task] => ZIO[R, Throwable, A]): ZIO[R, Throwable, A] =
    ZIO.runtime[R].flatMap { implicit rts =>
      BlazeClientBuilder[RIO[Any, *]](global).resource.toManagedZIO.use(f)
    }

  private def makeClient(ec: ExecutionContext)(implicit r: Runtime[Any]): ZManaged[Any, Throwable, Client[Task]] = {
    import zio.interop.catz._
    BlazeClientBuilder[Task](ec).resource.toManagedZIO
  }

  private def runHttp(conf: ApplicationConf): ZIO[AppEnvironment, Throwable, Unit] = {
    val apiRoutes = new ApiRoutes[AppEnvironment]()
    val externalApi = new ExternalApiRoutes[AppEnvironment]()
    val openApiRoutes = OpenApi.route[AppEnvironment](apiRoutes.endpoints ++ Seq(externalApi.jokeEndpoint))

    ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
      val rawRoute = RawHttp4sRoute.buildRaw[RIO[AppEnvironment, *]]()
      val allTapirRoutes = apiRoutes.getRoutes.foldK

      val httpApp: HttpApp[RIO[AppEnvironment, *]] =
        (rawRoute <+> allTapirRoutes <+> openApiRoutes <+> externalApi.route).orNotFound
      val httpAppExtended =
        Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

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
