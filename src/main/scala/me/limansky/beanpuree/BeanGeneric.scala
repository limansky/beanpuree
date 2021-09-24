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

/**
  * Represents an ability to convert bean of type B to generic representation ([[shapeless.HList]]). This is almost the
  * same with [[shapeless.Generic]], but for JavaBeans. E.g:
  *
  * {{{
  *   public class Cat {
  *       private String name;
  *       private int age;
  *
  *       public String getName() { return name };
  *       public void setName(String name) { this.name = name; }
  *
  *       public int getAge() { return age; }
  *       public void setAge(int age) { this.age = age; }
  *   }
  * }}}
  *
  * Then the BeanGeneric instance will have `Repr` = `String :: Int :: HNil`. Note that the HList order is the same with
  * the bean properties getters declaration order.
  *
  * Now it's possible to convert bean to HList, and then convert HList to something else.
  *
  * @tparam B
  *   the bean type
  */
trait BeanGeneric[B] {

  /** Generic representation type {B} ([[shapeless.HList]]) */
  type Repr

  /** Converts bean to generic representation. */
  def to(b: B): Repr

  /** Converts generic representation to bean instance */
  def from(r: Repr): B
}

/**
  * The companion object for [[BeanGeneric]] trait providing the way to obtain [[BeanGeneric]] instances.
  */
object BeanGeneric {

  /**
    * Provides representation of BeanGeneric with Repr type as a type parameter. This representation is useful in
    * implicit parameters capturing:
    *
    * {{{
    *   def convert[A, B, R <: HList](a: A)(implicit
    *     agen: BeanGeneric.Aux[A, R],
    *     bgen: Generic.Aux[B, R]): B = bgen.from(agen.to(a))
    * }}}
    *
    * @tparam B
    *   bean type
    * @tparam R
    *   generic representation type
    */
  type Aux[B, R] = BeanGeneric[B] { type Repr = R }

  /**
    * Provides a [[BeanGeneric]] instance for type {B} if possible.
    */
  def apply[B](implicit beanGeneric: BeanGeneric[B]): Aux[B, beanGeneric.Repr] = beanGeneric

  implicit def materialize[B, R]: Aux[B, R] = macro BeanGenericMacros.materialize[B, R]
}

class BeanGenericMacros(val c: whitebox.Context) extends CaseClassMacros with BeanUtils {
  import c.universe._

  def materialize[B: WeakTypeTag, R: WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[B]

    if (!isBean(tpe)) abort(s"$tpe is not a bean")

    val rtpe = beanReprTypTree(tpe)
    val ctorDtor = BeanCtorDtor(tpe)

    val b = TermName(c.freshName("bean"))
    val to = ctorDtor.deconstruct(b)

    val (rp, rts) = ctorDtor.reprBinding
    val from = cq" $rp => ${ctorDtor.construct(rts)} "

    q"""
      new _root_.me.limansky.beanpuree.BeanGeneric[$tpe] {
        override type Repr = $rtpe
        override def to($b: $tpe): Repr = $to
        override def from(r: Repr): $tpe = r match { case $from }
      }: _root_.me.limansky.beanpuree.BeanGeneric.Aux[$tpe, $rtpe]
    """
  }
}
