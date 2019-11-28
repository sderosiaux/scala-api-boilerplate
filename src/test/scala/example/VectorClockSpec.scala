package example

import cats.kernel.Monoid
import zio.test._
import zio.test.Assertion._
import cats.implicits._

case class Node(id: String)

case class Clock(epoch: Int) {

  def increment(): Clock =
    Clock(epoch + 1)
}

object Clock {

  implicit val clockOrdering = new Ordering[Clock] {
    override def compare(x: Clock, y: Clock): Int = x.epoch.compareTo(y.epoch)
  }
}

case class VectorClock(nodes: Map[Node, Clock]) {

  def increment(n: Node): VectorClock =
    VectorClock(nodes = if (!nodes.contains(n)) {
      nodes + (n -> Clock(1))
    } else {
      nodes + (n -> nodes(n).increment())
    })

  def merge(v: VectorClock): VectorClock =
    VectorClock(
      (nodes.toSeq ++ v.nodes.toSeq)
        .groupBy(_._1)
        .view
        .mapValues(_.map(_._2).max)
        .toMap
    )
}

object VectorClock {
  implicit val vcMonoid: Monoid[VectorClock] = Monoid.instance(VectorClock(Map()), _.merge(_))
}

object VectorClockSpec
    extends DefaultRunnableSpec(
      suite("The VectorClock")(
        test("should work") {
          val a = Node("a")
          val b = Node("b")
          val vc1 = VectorClock(Map()).increment(a).increment(a).increment(b)
          val vc2 = VectorClock(Map()).increment(b).increment(b).increment(a)
          assert(vc1 |+| vc2, equalTo(VectorClock(Map(a -> Clock(2), b -> Clock(2)))))
        }
      )
    )
