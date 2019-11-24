package example

import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.semiauto._
import pureconfig.{ ConfigReader, ConfigSource }
import zio.{ IO, ZIO }
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
import eu.timepit.refined.pureconfig._

final case class Port(port: PosInt)
final case class ApplicationConf(address: NonEmptyString, port: Port, pwd: String)

object ApplicationConf {
  implicit val portReader: ConfigReader[Port] = ConfigReader[PosInt].map { Port }
  implicit val confReader: ConfigReader[ApplicationConf] = deriveReader[ApplicationConf]

  def build(): IO[ConfigReaderFailures, ApplicationConf] = ZIO.fromEither(ConfigSource.default.load[ApplicationConf])
}
