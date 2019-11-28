package example.model

import io.circe.Decoder
import io.estatico.newtype.macros.newtype
import io.estatico.newtype.ops._

package object Joke {
  @newtype case class Joke(joke: String)

  object Joke {
    implicit val fromJson: Decoder[Joke] = deriving //implicitly[Decoder[String]].coerce
  }
}
