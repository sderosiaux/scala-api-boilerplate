package example.ziomodules

import zio.ZIO

trait QuoterService {
  val quoterService: QuoterService.Service[Any]
}

object QuoterService {

  trait Service[R] {
    def quote(): ZIO[R, Throwable, String]
  }

  def quote(): ZIO[QuoterService, Throwable, String] = ZIO.accessM[QuoterService](_.quoterService.quote())
}
