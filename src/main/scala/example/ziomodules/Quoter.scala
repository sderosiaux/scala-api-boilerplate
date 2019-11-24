package example.ziomodules

import zio.ZIO
import zio.macros.annotation.accessible

@accessible
trait Quoter {
  val quoter: Quoter.Service[Any]
}

object Quoter {

  trait Service[R] {
    def quote(): ZIO[R, Throwable, String]
  }
}
