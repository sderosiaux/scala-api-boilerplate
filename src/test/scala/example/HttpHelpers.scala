package example

import org.http4s.syntax.KleisliResponseOps
import org.http4s._
import zio.RIO
import zio.test.Assertion.equalTo
import zio.test.{ TestResult, assert }
import zio.interop.catz._

object HttpHelpers {
  def req[F[_]](path: String): Request[F] = Request[F](Method.GET, Uri.unsafeFromString(path))

  def assertReqRes[R, A](
    route: HttpRoutes[RIO[R, *]],
    path: String,
    result: A,
    expectedStatus: Status
  )(implicit de: EntityDecoder[RIO[R, *], A]): RIO[R, TestResult] =
    // manual conversion otherwise IntelliJ is lost with the implicit
    for {
      response <- new KleisliResponseOps(route).orNotFound.run(req(path))
      status = response.status
      data <- response.as[A]
    } yield assert(data, equalTo(result)) && assert(status, equalTo(expectedStatus))
}
