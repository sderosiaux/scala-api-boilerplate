package example

import cats.effect.ExitCode
import cats.implicits._
import example.route.{ ApiRoutes, ExternalApiRoutes, RawHttp4sRoute }
import example.ziomodules.{ QuoterService, RemoteQuoter }
import fs2.Stream.Compiler._
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.{ Logger => CLogger }
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{ CORS, Logger }
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console._
import zio.interop.catz._
import zio.{ App, RIO, ZIO, _ }

import scala.concurrent.ExecutionContext

object ComplexApp extends App {
  type AppEnvironment = Clock with Blocking with QuoterService

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    (for {
      conf <- ZIO.effect(ApplicationConf.build().orThrow())
      _ <- putStrLn(conf.toString)
      blockingEC <- blocking.blockingExecutor.map(_.asEC)
      server = ZIO.runtime[Clock with QuoterService].flatMap { implicit rts =>
        runHttpServer(conf)
      }
      _ <- ZIO.runtime[Any].flatMap { implicit rts =>
        makeClient(blockingEC).use { cc =>
          server.provideSome { currentEnv: Clock =>
            new Clock with RemoteQuoter { // Provide the Quoter to remove it from R
              override val client: Client[Task] = CLogger(logHeaders = true, logBody = false)(cc)
              override val clock: Clock.Service[Any] = currentEnv.clock
            }
          }
        }
      }
    } yield 0).foldM(h => putStrLn(h.toString).as(1), _ => ZIO.succeed(0))

  // Build a Managed Client to make REST calls
  // @R: independent
  private def makeClient(ec: ExecutionContext)(implicit r: Runtime[Any]): ZManaged[Any, Throwable, Client[Task]] = {
    import zio.interop.catz._
    BlazeClientBuilder[Task](ec).resource.toManagedZIO
  }

  // Start an HTTP Server
  // @R: Clock to run effects & Quoter for routes
  private def runHttpServer[R <: Clock with QuoterService](
    conf: ApplicationConf
  )(implicit rts: Runtime[R]): ZIO[R, Throwable, Unit] = {
    val apiRoutes = new ApiRoutes[R]()
    val externalApi = new ExternalApiRoutes[R]()
    val openApiRoutes = OpenApi.route[R](apiRoutes.endpoints ++ Seq(externalApi.jokeEndpoint))

    val rawRoute = RawHttp4sRoute.buildRaw[RIO[R, *]]()
    val allTapirRoutes = apiRoutes.getRoutes.foldK

    val httpApp: HttpApp[RIO[R, *]] =
      (rawRoute <+> allTapirRoutes <+> externalApi.route <+> openApiRoutes).orNotFound
    val httpAppExtended =
      CORS(Logger.httpApp(logHeaders = true, logBody = true)(httpApp))

    BlazeServerBuilder[RIO[R, *]] // ConcurrentEffect (from ZIO Runtime) + Timer (from ZIO Clock)
      .bindHttp(conf.port.port.value, conf.server.value)
      .withHttpApp(httpAppExtended)
      .withoutBanner
      .withSocketKeepAlive(true)
      .withTcpNoDelay(true)
      .serve
      .compile[RIO[R, *], RIO[R, *], ExitCode]
      .drain
  }
}
