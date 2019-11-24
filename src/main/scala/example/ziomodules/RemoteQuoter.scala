package example.ziomodules

import example.model.Joke
import io.circe.Decoder
import org.http4s.client.Client
import org.http4s.implicits._
import zio.interop.catz._
import zio.{ Task, UIO, ZIO }

trait RemoteQuoter extends Quoter {
  import io.circe.generic.semiauto._
  import org.http4s.circe.CirceEntityDecoder._

  implicit val jokeDecoder: Decoder[Joke] = deriveDecoder[Joke]

  val client: Client[Task]

  override val quoter = new Quoter.Service[Any] {

    override def quote(): UIO[String] =
      client
        .expect[Joke](uri"https://icanhazdadjoke.com/")
        .map(_.joke)
        .catchAll(h => ZIO.succeed(h.toString))
  }
}
