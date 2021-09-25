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

/**
  * Similar to [[BeanGeneric]], but like [[shapeless.LabelledGeneric]] adds field names information to Repr. Field names
  * are calculated from getters. E.g `getLoginDate` become `loginDate`, and `isActive` become `active`.
  *
  * For the bean from the [[BeanGeneric]] example the Repr type will be `'name -> String :: 'age -> Int :: HNil`.
  *
  * @tparam B
  *   bean type
  */
trait LabelledBeanGeneric[B] {

  /** Generic representation of B */
  type Repr

  /** Converts bean instance to generic value representation */
  def to(b: B): Repr

  /** Converts generic value into bean */
  def from(r: Repr): B
}

/**
  * Companion for [[LabelledBeanGeneric]]. Provides ability to obtain an instance of [[LabelledBeanGeneric]].
  */
object LabelledBeanGeneric {
  type Aux[B, R] = LabelledBeanGeneric[B] { type Repr = R }

  def apply[B](implicit gen: LabelledBeanGeneric[B]): Aux[B, gen.Repr] = gen

  implicit def beanLabelledGeneric[B, K <: HList, G <: HList, R <: HList](implicit
      lab: BeanLabelling.Aux[B, K],
      gen: BeanGeneric.Aux[B, G],
      zip: ZipWithKeys.Aux[K, G, R],
      ev: R <:< G
  ): Aux[B, R] = new LabelledBeanGeneric[B] {
    override type Repr = R
    override def to(b: B): R = zip(gen.to(b))

    override def from(r: R): B = gen.from(r)
  }
}
