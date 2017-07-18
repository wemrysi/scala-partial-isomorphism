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

import scala.{::, List, Nil, Unit, Some, None, Option}

import scalaz._, Scalaz._

object std {
  import PartialIso._

  // List
  def cons[A]: PartialIso[(A, List[A]), List[A]] =
    partial[(A, List[A]), List[A]]
      { case (a, as) => a :: as }
      { case a :: as => (a, as) }

  def nil[A]: PartialIso[Unit, List[A]] =
    partial[Unit, List[A]]
      { case _ => Nil }
      { case Nil => () }

  // Disjunction
  def left[A, B]: PartialIso[A, A \/ B] =
    partial[A, A \/ B]
      { case a => a.left }
      { case -\/(a) => a }

  def right[A, B]: PartialIso[B, A \/ B] =
    partial[B, A \/ B]
      { case b => b.right }
      { case \/-(b) => b }

  // Option
  def some[A]: PartialIso[A, Option[A]] =
    partial[A, Option[A]]
      { case a => Some(a) }
      { case Some(a) => a }

  def none[A]: PartialIso[Unit, Option[A]] =
    partial[Unit, Option[A]]
      { case _ => None }
      { case None => () }
}
