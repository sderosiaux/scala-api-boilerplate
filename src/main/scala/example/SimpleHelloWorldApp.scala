package example

import zio.console._
import zio.{ App, ZIO }

object SimpleHelloWorldApp extends App {
  def run(args: List[String]): ZIO[Console, Nothing, Int] =
    myAppLogic.as(0)

  val myAppLogic: ZIO[Console, Nothing, Unit] =
    for {
      _ <- putStrLn("Hello World")
    } yield ()
}
