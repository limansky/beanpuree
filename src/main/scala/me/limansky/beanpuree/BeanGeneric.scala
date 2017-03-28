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

  def apply[B](implicit beanGeneric: BeanGeneric[B]): BeanGeneric[B] = beanGeneric

  implicit def materialize[B, R]: Aux[B, R] = macro BeanGenericMacro.materialize[B, R]
}

trait BeanUtil { self: CaseClassMacros =>
  import c.universe._

  def beanReprTypTree(t: Type): Tree = {
    q"_root_.shapeles.HNil"
  }
}

@macrocompat.bundle
class BeanGenericMacro(val c: whitebox.Context) extends CaseClassMacros with BeanUtil {
  import c.universe._

  def materialize[B: WeakTypeTag, R: WeakTypeTag]: c.Expr[BeanGeneric.Aux[B, R]] = {
    val tpe = weakTypeOf[B]
//    if (tpe)


    val className = TypeName(c.freshName("anon$"))

    c.Expr(q"""
      final class $className extends _root_.me.limansky.BeanGeneric[$tpe] {
        override type Repr = ${beanReprTypTree(tpe)}
      }

      new $className(): _root_.me.limansky.BeanGeneric.Aux[$tpe, ${beanReprTypTree(tpe)}]
    """)
  }
}
