package example.route

import cats.implicits._
import org.http4s.HttpRoutes
import tapir._
import tapir.model.StatusCodes
import tapir.server.http4s._
import zio.interop.catz._
import zio.{ RIO, Task, ZIO }

class ApiRoutes[R]() {
  val getRoutes: List[HttpRoutes[RIO[R, *]]] = {
    implicit val customServerOptions: Http4sServerOptions[RIO[R, *]] = Http4sServerOptions
      .default[RIO[R, *]]

    val routeA: HttpRoutes[RIO[R, *]] = helloEndpoint.toRoutes {
      case (name, surname) =>
        ZIO.succeed(s"Hello, $name!${surname.map(s => s" (aka $s)").getOrElse("")}".asRight[Unit])
    }

    val routeB: HttpRoutes[RIO[R, *]] = byeEndpoint.toRoutes { name =>
      Task.effect(s"Bye, $name!".asLeft[String])
    }

    List(routeA, routeB)
  }

  val endpoints = List(byeEndpoint, helloEndpoint)

  def byeEndpoint: Endpoint[String, String, String, Nothing] =
    endpoint.get
      .in("bye" / path[String]("name").example("John"))
      .errorOut(
        oneOf(
          statusMapping(StatusCodes.InternalServerError, stringBody.description("The result you're going to get"))
        )
      )
      .out(stringBody)
      .description("This function always fails")

  def helloEndpoint: Endpoint[(String, Option[String]), Unit, String, Nothing] =
    endpoint.get
      .in("hello" / path[String]("name").example("John") / query[Option[String]]("surname").example("babar".some))
      .out(stringBody)
      .description("Say hello")
}
