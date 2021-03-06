package example.ziomodules

import example.model.joke.joke._
import example.{ ApplicationConf, ziomodules }
import org.http4s.client.Client
import zio.interop.catz._
import zio.{ Task, UIO, ZIO }

trait RemoteQuoter extends QuoterService {
  import org.http4s.circe.CirceEntityDecoder._

  val client: Client[Task]
  val config: ApplicationConf

  override val quoterService = new ziomodules.QuoterService.Service[Any] {

    override def quote(): UIO[String] =
      client
        .expect[Joke](config.jokerUrl.value)
        .map(_.joke)
        .catchAll(h => ZIO.succeed(h.toString))
  }
}
