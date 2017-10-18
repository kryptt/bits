package br
package bits

import scala.language.implicitConversions
import scala.concurrent.ExecutionContext

import cats._
import cats.effect._

import fs2.async.mutable.Topic

/** Extends a Piece with the ability to generate events as well as
  * simply recieving them.
  * This means a Block recieves a Topic instead of a stream
  * @tparam Model The piece of data held.
  * @tparam View The output generated as the model changes.
  * @constructor Creates a new block that subscribes and publishes to a given topic.
  * @param initialModel Initial Model for the piece.
  * @param update Partial function which provides event driven model updates (<code>Event -> Model -> IO[Model]</code>).
  * @param view Rendering method which taks a model instance and generates an output view.
  * @param topic Event pub/sub topic for dynamic component management.
  */
class Block[Model, View]
  (initialModel: IO[Model],
   update: PartialFunction[Any, Model => IO[Model]],
   view: Model => IO[View])
  (topic: Topic[IO, Any], maxQueue: Int = 8)
  (implicit eqM: Eq[Model], ec: ExecutionContext) {

  val piece = new Piece(initialModel, update, view)(topic.subscribe(maxQueue))

  def views = piece.views

  def publish[A](a: A) = topic.publish1(a).unsafeRunAsync {
    case Left(e) => throw e
    case Right(_) => ()
  }

}

object Block {
  implicit def piece[Model, View](block: Block[Model, View]): Piece[Model, View] = block.piece
  implicit def bit[View](block: Block[_, View]): Bit[View] = block.piece
}
