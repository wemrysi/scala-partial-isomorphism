/*
 * Copyright 2017 Emrys Ingersoll
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package partialiso

import scala.{Boolean, Option, PartialFunction, Some, None, Unit, List}
import scala.annotation.tailrec

import scalaz._, Scalaz._

final class PartialIso[A, B] (
  val papp: A => Option[B],
  val pinv: B => Option[A]
) {
  lazy val pappK: Kleisli[Option, A, B] =
    Kleisli(papp)

  lazy val pinvK: Kleisli[Option, B, A] =
    Kleisli(pinv)

  def andThen[C](other: PartialIso[B, C]): PartialIso[A, C] =
    other compose this

  def choice[C](other: => PartialIso[C, B]): PartialIso[A \/ C, B] =
    PartialIso[A \/ C, B] {
      case -\/(a) => papp(a)
      case \/-(c) => other.papp(c)
    } (b => pinv(b).map(_.left[C]) <+> other.pinv(b).map(_.right[A]))

  def compose[C](other: PartialIso[C, A]): PartialIso[C, B] =
    PartialIso(pappK <==< other.papp)(pinvK >==> other.pinv)

  def inverse: PartialIso[B, A] =
    PartialIso(pinv)(papp)

  def split[C, D](other: PartialIso[C, D]): PartialIso[(A, C), (B, D)] =
    PartialIso(pappK -*- other.pappK)(pinvK -*- other.pinvK)
}

object PartialIso extends PartialIsoInstances {
  // Constructors
  def apply[A, B](f: A => Option[B])(g: B => Option[A]): PartialIso[A, B] =
    new PartialIso(f, g)

  def partial[A, B](f: PartialFunction[A, B])(g: PartialFunction[B, A]): PartialIso[A, B] =
    apply(f.lift)(g.lift)

  def total[A, B](f: A => B)(g: B => A): PartialIso[A, B] =
    apply(f andThen (Some(_)))(g andThen (Some(_)))

  // Combinators
  def associate[A, B, C]: PartialIso[(A, (B, C)), ((A, B), C)] =
    total[(A, (B, C)), ((A, B), C)]
      { case (a, (b, c)) => ((a, b), c) }
      { case ((a, b), c) => (a, (b, c)) }

  def commute[A, B]: PartialIso[(A, B), (B, A)] =
    total((_: (A, B)).swap)(_.swap)

  def distribute[A, B, C]: PartialIso[(A, B \/ C), (A, B) \/ (A, C)] =
    total[(A, B \/ C), (A, B) \/ (A, C)] {
      case (a, bc) => bc.bimap((a, _), (a, _))
    } {
      case -\/((a, b)) => (a, b.left)
      case \/-((a, c)) => (a, c.right)
    }

  def element[A: Equal](a: A): PartialIso[Unit, A] =
    apply((_: Unit) => Some(a))(x => (x === a).option(()))

  def foldl[A, B](iso: PartialIso[(A, B), A]): PartialIso[(A, List[B]), A] = {
    val step = (id[A] -*- std.cons[B].inverse) >>> associate >>> (iso -*- id)
    iterate(step) >>> (id -*- std.nil.inverse) >>> unit.inverse
  }

  def id[A]: PartialIso[A, A] =
    total[A, A](a => a)(a => a)

  def ignore[A](a: A): PartialIso[A, Unit] =
    total((_: A) => ())(_ => a)

  def iterate[A](iso: PartialIso[A, A]): PartialIso[A, A] = {
    @tailrec
    def driver(f: A => Option[A])(a0: A): A =
      f(a0) match {
        case Some(a1) => driver(f)(a1)
        case None     => a0
      }

    total(driver(iso.papp))(driver(iso.pinv))
  }

  def subset[A](p: A => Boolean): PartialIso[A, A] = {
    val f = (a: A) => p(a).option(a)
    PartialIso(f)(f)
  }

  def unit[A]: PartialIso[A, (A, Unit)] =
    total((_: A, ()))(_._1)
}

sealed abstract class PartialIsoInstances {
  implicit val partialIsoCategorical: Choice[PartialIso] with Split[PartialIso] =
    new Choice[PartialIso] with Split[PartialIso] {
      def id[A] = PartialIso.id[A]
      def compose[A, B, C](f: PartialIso[B, C], g: PartialIso[A, B]) = f compose g
      def split[A, B, C, D](f: PartialIso[A, B], g: PartialIso[C, D]) = f split g
      def choice[A, B, C](f: => PartialIso[A, C], g: => PartialIso[B, C]) = f choice g
    }
}
