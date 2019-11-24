package example.route

import cats.implicits._
import example.ziomodules.QuoterService
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import tapir.json.circe._
import tapir.model.StatusCodes
import tapir.server.http4s._
import tapir.{ Endpoint, endpoint, statusMapping, stringBody, _ }
import zio.RIO
import zio.interop.catz._
case class JokeFailure(joke: String)

class ExternalApiRoutes[R <: QuoterService] {

  val jokeEndpoint: Endpoint[Unit, JokeFailure, String, Nothing] =
    endpoint.get
      .in("joke")
      .out(stringBody)
      .errorOut(
        oneOf(
          statusMapping(
            StatusCodes.InternalServerError,
            jsonBody[JokeFailure].description("The result you're going to get")
          )
        )
      )
      .description("Have fun")

  val route: HttpRoutes[RIO[R, *]] =
    jokeEndpoint.toRoutes { _ =>
      QuoterService.quote().map(JokeFailure(_).asLeft[String])
    }
}
