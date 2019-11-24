package example

import cats.effect.Sync
import example.route.ApiRoutes
import zio.{ RIO, Task, UIO, ZIO }
import zio.interop.catz._
import zio.interop.catz.core._
import zio.test._
import zio.test.Assertion._
import org.http4s._

object HttpHelpers {
  def req[F[_]](path: String): Request[F] = Request[F](Method.GET, Uri.unsafeFromString(path))
}

object HelloSpec
    extends DefaultRunnableSpec(
      suite("Hello")(
        testM("should work") {
          import org.http4s.implicits._
          val req: Request[Task] = HttpHelpers.req[Task]("/hello/john")
          val x = new ApiRoutes[Any]().getRoutes.head.orNotFound
          val res: RIO[Any, Response[Task]] = x.run(req)

          res.flatMap { res =>
            val ress = res.as[String]
            assertM(ress, equalTo("Hello, john!"))
          }
        },
        testM("should work") {
          UIO.effectTotal(assert("hello", equalTo("hello")))
        }
      )
    )
