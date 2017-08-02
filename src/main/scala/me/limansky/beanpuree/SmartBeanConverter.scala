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

trait SmartBeanConverter[B, P] {
  def productToBean(p: P): B
  def beanToProduct(b: B): P
}

object SmartBeanConverter {

  def apply[B, P](implicit sbc: SmartBeanConverter[B, P]): SmartBeanConverter[B, P] = sbc

  implicit def converter[B, BR <: HList, BA <: HList, P, PR <: HList, PK <: HList](implicit
    gen: LabelledGeneric.Aux[P, PR],
    pKeys: Keys.Aux[PR, PK],
    bgen: LabelledBeanGeneric.Aux[B, BR],
    align: AlignByKeys.Aux[BR, PK, BA],
    mapper: JavaTypeMapper[BA, PR],

  ): SmartBeanConverter[B, P] = new SmartBeanConverter[B, P] {

    override def productToBean(p: P): B = ???

    override def beanToProduct(b: B): P = ???
  }

}