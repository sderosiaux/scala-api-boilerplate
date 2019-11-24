package example

import zio.test.Gen

object Generators {
  val nonEmptyStrings = Gen.medium(Gen.stringN(_)(Gen.alphaNumericChar), 1)
}
