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

import shapeless.{CaseClassMacros, DepFn0, HList, SingletonTypeUtils}

import scala.language.experimental.macros
import scala.reflect.macros.whitebox


trait BeanLabelling[T] extends DepFn0 with Serializable { type Out <: HList }

object BeanLabelling {
  type Aux[T, O] = BeanLabelling[T] { type Out = O }

  def apply[T](implicit lab: BeanLabelling[T]): Aux[T, lab.Out] = lab

  implicit def beanLabelling[T]: BeanLabelling[T] = macro BeanLabellingMacros.mkLabelling[T]
}

@macrocompat.bundle
class BeanLabellingMacros(val c: whitebox.Context) extends SingletonTypeUtils with CaseClassMacros with BeanUtils {
  import c.universe._

  def mkLabelling[T : WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[T]

    if (!isBean(tpe)) abort(s"$tpe is not a bean")

    val labels = propsOf(tpe).map(f => nameAsString(f._1))

    val labelTypes = labels.map(SingletonSymbolType(_))
    val labelValues = labels.map(mkSingletonSymbol)

    val labelsType = mkHListTpe(labelTypes)
    val labelsValue = labelValues.foldRight(q"_root_.shapeless.HNil": Tree) {
      case (elem, acc) => q"_root_.shapeless.::($elem, $acc)"
    }

    q"""
       new _root_.me.limansky.beanpuree.BeanLabelling[$tpe] {
         override type Out = $labelsType
         def apply(): $labelsType = $labelsValue
       }: _root_.me.limansky.beanpuree.BeanLabelling.Aux[$tpe, $labelsType]
     """
  }
}
