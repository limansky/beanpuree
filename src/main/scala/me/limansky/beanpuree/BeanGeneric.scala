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

import shapeless.{CaseClassMacros, HList}

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
trait BeanUtil { self: CaseClassMacros =>
  import c.universe._

  def beanReprTypTree(tpe: Type): Tree = {
    mkHListTypTree(propsOf(tpe).map(_._2))
  }

  object Getter {
    val getterPattern = "^(is|get)(.*)".r
    def unapply[T <: Symbol](sym: T): Option[(String, T)] = {
      val nameStr = sym.name.toString
      for {
        getterPattern(_, name) <- getterPattern.findFirstIn(nameStr)
      } yield (name, sym)
    }
  }

  object BeanCtorDtor {
    def apply(tpe: Type): CtorDtor = {

      val (gs, ss) = gettersAndSetters(tpe)

      val instance = TermName(c.freshName("ins"))

      val elems = gs.map(_ => TermName(c.freshName("pat")))

      val reprPattern =
        elems.foldRight(q"_root_.shapeless.HNil": Tree) {
          case (bound, acc) => pq"_root_.shapeless.::($bound, $acc)"
        }

      new CtorDtor {
        def construct(args: List[Tree]): Tree = {
          val setters = ss.zip(args).map { case (s, a) => q"$instance.$s($a)" }

          q"""
            val $instance = new $tpe
            ..$setters
            $instance
          """
        }
        def binding: (Tree, List[Tree]) = (pq"x", Nil)
        def reprBinding: (Tree, List[Tree]) = (reprPattern, elems.map(e => q"$e"))
      }
    }
  }

  def gettersAndSetters(tpe: Type): (List[MethodSymbol], List[MethodSymbol]) = {
    val methods = tpe.decls.toList collect {
      case sym: MethodSymbol if sym.isMethod => sym
    }

    val byName = methods.map(s => s.name.toString -> s).toMap

    val getters = methods.collect {
      case Getter(x @ (_, _)) => x
    }.filter(x => byName.contains("set" + x._1))

    val setters = getters.map(x => byName("set" + x._1))
    (getters.map(_._2), setters)
  }

  def propsOf(tpe: Type): List[(TermName, Type)] = {
    val (getters, _) = gettersAndSetters(tpe)
    getters.map(sym => (sym.name.toTermName, sym.typeSignatureIn(tpe).finalResultType))
  }
}

@macrocompat.bundle
class BeanGenericMacros(val c: whitebox.Context) extends CaseClassMacros with BeanUtil {
  import c.universe._

  def materialize[B: WeakTypeTag, R: WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[B]
    val rtpe = beanReprTypTree(tpe)
    println(s"$rtpe")
    val ctorDtor = BeanCtorDtor(tpe)

    val (p, ts) = ctorDtor.binding
    val to = cq""" $p => ${mkHListValue(ts)} """

    val (rp, rts) = ctorDtor.reprBinding
    val from = cq" $rp => ${ctorDtor.construct(rts)} "

    val className = TypeName(c.freshName("anon$"))

    val tree = q"""
      final class $className extends _root_.me.limansky.beanpuree.BeanGeneric[$tpe] {
        override type Repr = $rtpe
        override def to(b: $tpe): Repr = (b match { case $to }).asInstanceOf[Repr]
        override def from(r: Repr): $tpe = r match { case $from }
      }

      new $className: _root_.me.limansky.beanpuree.BeanGeneric.Aux[$tpe, $rtpe]
    """

    println("TREE: \n" + showCode(tree))

    tree
  }
}
