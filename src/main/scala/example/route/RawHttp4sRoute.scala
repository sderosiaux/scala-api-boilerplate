package example.route

import cats.effect.Sync
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object RawHttp4sRoute {

  def buildRaw[F[_]: Sync](): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes
      .of[F] {
        case GET -> Root / "stuff" => Ok("Good stuff")
      }
  }
}
