/*
 * Copyright 2017 Mike Limansky
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

package me.limansky.beanpuree

import shapeless.ops.record.Remover
import shapeless.{::, DepFn1, HList, HNil}
import shapeless.labelled.{FieldType, field}

trait AlignByKeys[T <: HList, K <: HList] extends DepFn1[T] {
  override type Out <: HList
}

object AlignByKeys {
  type Aux[T <: HList, K <: HList, O] = AlignByKeys[T, K] { type Out = O}

  def apply[T <: HList, K <: HList](implicit ev: AlignByKeys[T, K]): Aux[T, K, ev.Out] = ev

  implicit val hnilAlign: AlignByKeys[HNil, HNil] = new AlignByKeys[HNil, HNil] {
    override type Out = HNil
    override def apply(t: HNil): HNil = HNil
  }

  implicit def hlistAling[T <: HList, KH, KT <: HList, V, R <: HList, TA <: HList](implicit
    remover: Remover.Aux[T, KH, (V, R)],
    tailAlign: AlignByKeys.Aux[R, KT, TA]
  ): Aux[T, KH :: KT, FieldType[KH, V] :: TA] = new AlignByKeys[T, KH :: KT] {
    override type Out = FieldType[KH, V] :: TA

    override def apply(t: T): FieldType[KH, V] :: TA = {
      val (v, r) = remover(t)
      field[KH](v) :: tailAlign(r)
    }
  }
}
