package example.ziomodules

import io.circe.Decoder
import org.http4s.client.Client
import zio.{ RIO, Task, UIO, ZIO }
import org.http4s.implicits._
import org.http4s.circe._
import org.http4s.EntityDecoder
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
  val client: Client[Task]

  // we need to summon a Sync[Task] (hence a MonadError[?, Throwable])
  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[Task, A] = jsonOf[Task, A]

  override val simple = new StringServicesModule.StringProvider[Any] {
    override def get: UIO[String] = // R=Any
      client
        .expect[String](uri"https://icanhazdadjoke.com/")
        .catchAll(h => ZIO.succeed(h.toString))
  }
}
