package example.route

import cats.implicits._
import example.ziomodules.StringServicesModule
import org.http4s.HttpRoutes
import tapir.server.http4s._
import tapir.{ Endpoint, endpoint, stringBody }
import zio.RIO
import zio.interop.catz._

class ExternalApiRoutes[R <: StringServicesModule] {

  val jokeEndpoint: Endpoint[Unit, Unit, String, Nothing] =
    endpoint.get
      .out(stringBody)
      .description("Have fun")

  val route: HttpRoutes[RIO[R, *]] =
    jokeEndpoint.toRoutes { _ =>
      StringServicesModule.get.map(_.asRight[Unit])
    }
}
