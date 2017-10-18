package br
package bits

import scala.language.implicitConversions
import scala.concurrent.ExecutionContext

import cats._
import cats.effect._

import fs2._

/** Most basic rendering component developers are expected to extend.
  * There are three methods to implement:
  *  1. <b>initialModel</b> <i>~></i> The initial data model represented by a given Piece.
  *  1. <b>view</b> <i>~></i> The method that 'renders'.
  *  1. <b>update</b> <i>~></i> A partial function from events to update functions. <code>events -> model -> IO[model]</code>
  *
  * It is very simple to construct a new piece:
  *  @example
  *    {{{
  *    val counter = new Piece[Int, String] (
  *      initialModel = IO(0),
  *      update = { case Reset => (_ => IO(0))
  *                 case Add(a): Int    => (c => IO(c + a)) },
  *      view = (cnt => s"You are viewing a Counter.\\nCount is: \$cnt")
  *      )(events)
  *    }}}
  *
  * @example
  *   {{{
  *   val hello = Piece.const(IO("Hello world!"))
  *   }}}
  *
  * Composing bits can be straight forward as well:
  * @example
  *   {{{
  *   val both = Bit.fuse(hello, counter)(_ + ".\n" + _)
  *   }}}
  *
  * @tparam Model The piece of data held.
  * @tparam View The output generated as the model changes.
  * @constructor Creates a new piece that is responsible for processing a given event stream.
  * @param initialModel Initial Model for the piece.
  * @param update Partial function which provides event driven model updates (<code>Event -> Model -> IO[Model]</code>).
  * @param view Rendering method which taks a model instance and generates an output view.
  */
class Piece[Model, View]
  (initialModel: IO[Model],
   update: PartialFunction[Any, Model => IO[Model]],
   view: Model => IO[View])
  (events: Stream[IO, Any])
  (implicit eqM: Eq[Model], ec: ExecutionContext) {

  private type Ref = async.Ref[IO, Model]
  private type UpdateFn    = Model => IO[Model]

  /** Resulting stream of views as the incoming events stream drives
    the rendering of internal model changes. */
  def views: Stream[IO, View] = updates.changes.evalMap(view)

  private def updates: Stream[IO, Model] = Stream.force {
    initialModel.flatMap(async.refOf[IO, Model]).map {
      ms => events.collect(update).evalMap(_update(ms, _))
    }
  }

  private def _update(ref: Ref, update: UpdateFn): IO[Model] = for {
    curr <- ref.get
    next <- update(curr)
    _    <- ref.setAsyncPure(next)
  } yield next

}

object Piece {
  def apply[Model, View](
    initialModel: IO[Model],
    update: PartialFunction[Any, Model => IO[Model]],
    view: Model => IO[View])
           (events: Stream[IO, Any])
           (implicit eqM: Eq[Model], ec: ExecutionContext): Piece[Model, View] =
    new Piece(initialModel, update, view)(events)

  def const[View](
    view: => IO[View])
           (implicit ec: ExecutionContext): Piece[Unit, View] =
    new Piece[Unit, View](
      IO.unit, PartialFunction.empty, _ => view)(
      Stream.constant(()))(
      Eq.allEqual[Unit], ec)

  implicit def bit[View](piece: Piece[_, View]) = new Bit[View] {
    def views = piece.views
  }
}
