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

@macrocompat.bundle
trait BeanUtils { self: CaseClassMacros =>
  import c.universe._

  private val getterPattern = "^(is|get)(.+)".r

  case class Field(name: String, getter: MethodSymbol, setter: MethodSymbol)

  def beanReprTypTree(tpe: Type): Tree = {
    mkHListTypTree(beanFields(tpe).map(_.getter.typeSignatureIn(tpe).finalResultType))
  }

  def mayBeGetter(sym: MethodSymbol): Option[(String, MethodSymbol)] = {
    if (sym.paramLists.isEmpty || sym.paramLists == List(List())) {
      val nameStr = sym.name.toString
      for {
        getterPattern(_, name) <- getterPattern.findFirstIn(nameStr)
      } yield (name, sym)
    } else None
  }

  def isSetter(sym: MethodSymbol, expectedTpe: Type): Boolean = {
    sym.typeSignature.finalResultType =:= typeOf[Unit] &&
      (sym.paramLists match {
        case List(List(t)) => t.typeSignature =:= expectedTpe
        case _ => false
      })
  }

  trait BeanCtorDtor {
    def construct(args: List[Tree]): Tree
    def deconstruct(bean: TermName): Tree
    def reprBinding: (Tree, List[Tree])
  }

  object BeanCtorDtor {
    def apply(tpe: Type): BeanCtorDtor = {

      val fs = beanFields(tpe)

      val elems = fs.map(_ => TermName(c.freshName("pat")))

      val reprPattern =
        elems.foldRight(q"_root_.shapeless.HNil": Tree) {
          case (bound, acc) => pq"_root_.shapeless.::($bound, $acc)"
        }

      new BeanCtorDtor {
        def construct(args: List[Tree]): Tree = {
          val bean = TermName(c.freshName("bean"))
          val setters = fs.zip(args).map { case (Field(_, _, s), a) => q"$bean.$s($a)" }

          q"""
            val $bean = new $tpe
            ..$setters
            $bean
          """
        }

        def deconstruct(bean: TermName): Tree = mkHListValue(fs.map(f => q"$bean.${f.getter}"))

        def reprBinding: (Tree, List[Tree]) = (reprPattern, elems.map(e => q"$e"))
      }
    }
  }

  def beanFields(tpe: Type): List[Field] = {
    val methods = tpe.decls.toList collect {
      case sym: MethodSymbol if sym.isMethod && sym.isPublic => sym
    }

    val byName = methods.map(s => s.name.toString -> s).toMap

    val getters = methods
      .flatMap(mayBeGetter)
      .filter(x => byName.get("set" + x._1)
        .exists(sym => isSetter(sym, x._2.typeSignatureIn(tpe).finalResultType))
      )

    getters.map { case (name, getter) => Field(firstLower(name), getter, byName("set" + name))}
  }

  def isBean(tpe: Type): Boolean = {
    val sym = tpe.typeSymbol.asType
    !isReprType(tpe) &&
      !sym.isAbstract &&
      tpe.decls.exists(x => x.isConstructor && x.isPublic && x.typeSignature.paramLists == List(List()))
  }

  private def firstLower(s: String): String = {
    if (s.isEmpty) s else s.head.toLower +: s.tail
  }
}
