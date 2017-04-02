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

import shapeless.CaseClassMacros

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

trait BeanGeneric[B] {
  type Repr
  def to(b: B): Repr
  def from(r: Repr): B
}

object BeanGeneric {
  type Aux[B, R] = BeanGeneric[B] { type Repr = R }

  def apply[B](implicit beanGeneric: BeanGeneric[B]): Aux[B, beanGeneric.Repr] = beanGeneric

  implicit def materialize[B, R]: Aux[B, R] = macro BeanGenericMacros.materialize[B, R]
}

@macrocompat.bundle
class BeanGenericMacros(val c: whitebox.Context) extends CaseClassMacros with BeanUtils {
  import c.universe._

  def materialize[B: WeakTypeTag, R: WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[B]

    if (!isBean(tpe)) abort(s"$tpe is not a bean")

    val rtpe = beanReprTypTree(tpe)
    val ctorDtor = BeanCtorDtor(tpe)

    val (p, ts) = ctorDtor.binding
    val to = cq""" $p => ${mkHListValue(ts)} """

    val (rp, rts) = ctorDtor.reprBinding
    val from = cq" $rp => ${ctorDtor.construct(rts)} "

    q"""
      new _root_.me.limansky.beanpuree.BeanGeneric[$tpe] {
        override type Repr = $rtpe
        override def to(b: $tpe): Repr = (b match { case $to }).asInstanceOf[Repr]
        override def from(r: Repr): $tpe = r match { case $from }
      }: _root_.me.limansky.beanpuree.BeanGeneric.Aux[$tpe, $rtpe]
    """
  }
}
