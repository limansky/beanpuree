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

import shapeless.HList
import shapeless.ops.hlist.ZipWithKeys


trait LabelledBeanGeneric[T] {
  type Repr
  def to(t : T) : Repr
  def from(r : Repr) : T
}

object LabelledBeanGeneric {
  type Aux[T, R] = LabelledBeanGeneric[T] { type Repr = R }

  def apply[T](implicit gen: LabelledBeanGeneric[T]): Aux[T, gen.Repr] = gen

  implicit def beanLabelledGeneric[T, K <: HList, G <: HList, R <: HList](implicit
    lab: BeanLabelling.Aux[T, K],
    gen: BeanGeneric.Aux[T, G],
    zip: ZipWithKeys.Aux[K, G, R],
    ev: R <:< G
  ): Aux[T, R] = new LabelledBeanGeneric[T] {
    override type Repr = R
    override def to(t: T): R = zip(gen.to(t))

    override def from(r: R): T = gen.from(r)
  }
}
