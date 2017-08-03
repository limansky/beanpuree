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

import shapeless.{HList, LabelledGeneric}
import shapeless.ops.record.Keys

/**
  * Converts bean to product type (case class) and backwards.
  *
  * Doesn't care about fields order. The fields have to have compatible types.
  *
  * @see [[LabelledBeanGeneric]], [[JavaTypeMapper]]
  * @tparam B bean type
  * @tparam P product type type
  */
trait BeanConverter[B, P] {
  /** Converts product instance to bean */
  def productToBean(p: P): B

  /** Converts bean instance to product */
  def beanToProduct(b: B): P
}

object BeanConverter {

  def apply[B, P](implicit sbc: BeanConverter[B, P]): BeanConverter[B, P] = sbc

  implicit def converter[B, BR <: HList, BK <: HList, BA <: HList, P, PR <: HList, PK <: HList](implicit
    gen: LabelledGeneric.Aux[P, PR],
    pKeys: Keys.Aux[PR, PK],
    bgen: LabelledBeanGeneric.Aux[B, BR],
    bKeys: Keys.Aux[BR, BK],
    align: AlignByKeys.Aux[BR, PK, BA],
    mapper: JavaTypeMapper[BA, PR],
    revAlign: AlignByKeys.Aux[BA, BK, BR]
  ): BeanConverter[B, P] = new BeanConverter[B, P] {

    override def productToBean(p: P): B = bgen.from(revAlign(mapper.scalaToJava(gen.to(p))))

    override def beanToProduct(b: B): P = gen.from(mapper.javaToScala(align(bgen.to(b))))
  }

}
