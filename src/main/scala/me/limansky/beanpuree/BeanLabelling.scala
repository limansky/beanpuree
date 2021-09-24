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

import shapeless.{ CaseClassMacros, DepFn0, HList, SingletonTypeUtils }

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
  * Represents ability to extract labels, like [[shapeless.DefaultSymbolicLabelling]] but for JavaBeans.
  *
  * @tparam B
  *   bean to extract labels
  */
trait BeanLabelling[B] extends DepFn0 with Serializable { type Out <: HList }

object BeanLabelling {
  type Aux[B, O] = BeanLabelling[B] { type Out = O }

  def apply[B](implicit lab: BeanLabelling[B]): Aux[B, lab.Out] = lab

  implicit def beanLabelling[B]: BeanLabelling[B] = macro BeanLabellingMacros.mkLabelling[B]
}

class BeanLabellingMacros(val c: whitebox.Context) extends SingletonTypeUtils with CaseClassMacros with BeanUtils {
  import c.universe._

  def mkLabelling[B: WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[B]

    if (!isBean(tpe)) abort(s"$tpe is not a bean")

    val labels = beanFields(tpe).map(_.name)

    val labelTypes = labels.map(SingletonSymbolType(_))
    val labelsType = mkHListTpe(labelTypes)

    val labelValues = labels.map(mkSingletonSymbol)
    val labelsValue = mkHListValue(labelValues)

    q"""
       new _root_.me.limansky.beanpuree.BeanLabelling[$tpe] {
         override type Out = $labelsType
         def apply(): $labelsType = $labelsValue
       }: _root_.me.limansky.beanpuree.BeanLabelling.Aux[$tpe, $labelsType]
     """
  }
}
