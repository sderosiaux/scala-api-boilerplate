package example.model.joke

import io.circe._
import io.estatico.newtype.macros.newtype
import io.estatico.newtype.ops._

object joke {
  @newtype case class Joke(joke: String)

  object Joke {
    implicit val fromJson: Decoder[Joke] = deriving // implicitly[Decoder[String]].coerce
  }
}
