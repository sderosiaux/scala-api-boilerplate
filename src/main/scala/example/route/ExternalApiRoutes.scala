package example.route

import cats.implicits._
import example.ziomodules.Quoter
import org.http4s.HttpRoutes
import tapir.server.http4s._
import tapir.{ Endpoint, endpoint, stringBody }
import zio.{ RIO, ZIO }
import zio.interop.catz._

class ExternalApiRoutes[R <: Quoter] {

  val jokeEndpoint: Endpoint[Unit, Unit, String, Nothing] =
    endpoint.get
      .in("joke")
      .out(stringBody)
      .description("Have fun")

  val route: HttpRoutes[RIO[R, *]] =
    jokeEndpoint.toRoutes { _ =>
      ZIO.accessM[Quoter](_.quoter.quote()).map(_.asRight[Unit])
    // Quoter.>.get().map(_.asRight[Unit])
    }
}
