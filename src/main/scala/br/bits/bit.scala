package br
package bits

import cats.effect.IO
import fs2._

import scala.concurrent.ExecutionContext

trait Bit[View] {
  def views: Stream[IO, View]
}

object Bit {
  def apply[View](v: Stream[IO, View]): Bit[View] = new Bit[View] { def views = v }

  def fuse[A, B, View](a: Bit[A], b: Bit[B])(view: (A, B) => IO[View])(implicit ec: ExecutionContext): Bit[View] =
    new Bit[View] {
      def views = {
        val sa  = a.views.head.flatMap(async.hold(_, a.views.tail)).flatMap(_.continuous)
        val sb  = b.views.head.flatMap(async.hold(_, b.views.tail)).flatMap(_.continuous)
        val sig = sa.zipWith(sb)(view).evalMap(identity)
        a.views.merge(b.views).flatMap(_ => sig.head)
      }
    }
}
