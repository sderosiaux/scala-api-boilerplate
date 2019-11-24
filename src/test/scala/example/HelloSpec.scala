package example

import zio.UIO
import zio.test._
import zio.test.Assertion._

object HelloSpec
    extends DefaultRunnableSpec(
      suite("Hello")(test("should work") {
        assert("hello", equalTo("hello"))
      }, testM("should work") {
        UIO.effectTotal(assert("hello", equalTo("hello")))
      })
    )
