package example.ziomodules

import example.model.Joke
import io.circe.Decoder
import org.http4s.client.Client
import org.http4s.implicits._
import zio.{ RIO, Task, UIO, ZIO }
import zio.interop.catz._

trait StringServicesModule extends Serializable {
  val simple: StringServicesModule.StringProvider[Any]
}

object StringServicesModule {
  trait StringProvider[R] {
    def get: RIO[R, String] // R=Any
  }

  def get: RIO[StringServicesModule, String] = ZIO.accessM(_.simple.get) // R=StringProvider
}

trait RemoteStringServicesModule extends StringServicesModule {
  import io.circe.generic.semiauto._
  import org.http4s.circe.CirceEntityDecoder._

  implicit val jokeDecoder: Decoder[Joke] = deriveDecoder[Joke]

  val client: Client[Task]

  override val simple = new StringServicesModule.StringProvider[Any] {
    override def get: UIO[String] = // R=Any
      client
        .expect[Joke](uri"https://icanhazdadjoke.com/")
        .map(_.joke)
        .catchAll(h => ZIO.succeed(h.toString))
  }
}
