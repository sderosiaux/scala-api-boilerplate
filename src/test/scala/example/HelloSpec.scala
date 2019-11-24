package example

import example.Generators.nonEmptyStrings
import example.HttpHelpers.assertReqRes
import example.route.ApiRoutes
import org.http4s._
import zio.interop.catz._
import zio.test._

object HelloSpec
    extends DefaultRunnableSpec(
      suite("The API")(
        testM("should hello anyone") {
          val x = new ApiRoutes[Any]().getRoutes.head
          checkM(nonEmptyStrings) { s =>
            assertReqRes(x, s"/hello/$s", s"Hello, $s!", Status.Ok)
          }
        },
        testM("should fail on bye") {
          val x = new ApiRoutes[Any]().getRoutes.tail.head
          checkM(nonEmptyStrings) { s =>
            assertReqRes(x, s"/bye/$s", s"Bye, $s!", Status.InternalServerError)
          }
        }
      )
    )

// test avec un TestQuoter
