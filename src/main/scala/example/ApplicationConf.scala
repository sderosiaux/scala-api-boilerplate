package example

import cats.implicits._
import ciris._
import ciris.api.Id
import ciris.generic._
import ciris.refined._
import eu.timepit.refined._
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString

final case class Port(port: PosInt)

object Port {

  implicit val decoder: ConfigDecoder[String, Port] =
    ConfigDecoder[String, PosInt].mapOption("Port")(Port(_).some)
}

final case class ApplicationConf(server: NonEmptyString, port: Port, password: Secret[String])

object ApplicationConf {
  private val address: ConfigValue[Id, NonEmptyString] = env[NonEmptyString]("address").orValue(refineMV("localhost"))
  private val port = env[Port]("port").orValue(Port(refineMV(8000)))
  private val pwd = env[Secret[String]]("pwd").orValue(Secret(""))

  def build(): ConfigResult[ciris.api.Id, ApplicationConf] =
    // TODO: update to ciris 1.x to handle cats mapN
    // val config = (address, port).mapN(ApplicationConf.apply)
    ciris.loadConfig(address, port, pwd)(ApplicationConf.apply)
}
