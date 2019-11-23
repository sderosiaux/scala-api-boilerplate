package example

import cats.implicits._
import org.http4s.HttpRoutes
import tapir.Endpoint
import tapir.docs.openapi._
import tapir.openapi.circe.yaml._
import tapir.openapi.{ Contact, Info }
import tapir.swagger.http4s.SwaggerHttp4s
import zio.RIO
import zio.interop.catz._

object OpenApi {

  def route[R](endpoints: List[Endpoint[_, _, _, _]]): HttpRoutes[RIO[R, *]] = {
    val openApi = endpoints.toOpenAPI(
      Info("My App", "1.0.0", "My great App".some, contact = Contact("@sderosiaux".some, email = None, url = None).some)
    )
    new SwaggerHttp4s(openApi.toYaml, "", "openapi.yaml").routes[RIO[R, *]]
  }
}
