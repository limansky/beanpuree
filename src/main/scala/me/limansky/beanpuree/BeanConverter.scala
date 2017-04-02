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

import shapeless.ops.hlist.Align
import shapeless.{HList, LabelledGeneric}

/**
  * Converts bean to product type (case class) and backwards.
  *
  * Doesn't care about fields order. Only requires that the fields have same type and same name.
  *
  * @see [[LabelledBeanGeneric]]
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
  def apply[B, S](implicit beanConverter: BeanConverter[B, S]): BeanConverter[B, S] = beanConverter

  implicit def converter[B, P, BR <: HList, PR <: HList](implicit
    gen: LabelledGeneric.Aux[P, PR],
    bgen: LabelledBeanGeneric.Aux[B, BR],
    align: Align[BR, PR],
    revAlign: Align[PR, BR]
  ): BeanConverter[B, P] = new BeanConverter[B, P] {
    override def productToBean(p: P): B = bgen.from(revAlign(gen.to(p)))
    override def beanToProduct(b: B): P = gen.from(align(bgen.to(b)))
  }
}
