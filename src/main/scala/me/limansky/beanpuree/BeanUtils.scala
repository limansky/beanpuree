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

  def beanReprTypTree(tpe: Type): Tree = {
    mkHListTypTree(propsOf(tpe).map(_._2))
  }

  object Getter {
    val getterPattern = "^(is|get)(.+)".r
    def unapply(sym: MethodSymbol): Option[(String, MethodSymbol)] = {
      if (sym.paramLists.isEmpty || sym.paramLists == List(List())) {
        val nameStr = sym.name.toString
        for {
          getterPattern(_, name) <- getterPattern.findFirstIn(nameStr)
        } yield (name, sym)
      } else None
    }
  }

  object BeanCtorDtor {
    def apply(tpe: Type): CtorDtor = {

      val (gs, ss) = gettersAndSetters(tpe)

      val bean = TermName(c.freshName("bean"))
      val elems = gs.map(_ => TermName(c.freshName("pat")))

      val reprPattern =
        elems.foldRight(q"_root_.shapeless.HNil": Tree) {
          case (bound, acc) => pq"_root_.shapeless.::($bound, $acc)"
        }

      new CtorDtor {
        def construct(args: List[Tree]): Tree = {
          val setters = ss.zip(args).map { case (s, a) => q"$bean.$s($a)" }

          q"""
            val $bean = new $tpe
            ..$setters
            $bean
          """
        }
        def binding: (Tree, List[Tree]) = (pq"$bean", gs.map(g => q"$bean.$g"))
        def reprBinding: (Tree, List[Tree]) = (reprPattern, elems.map(e => q"$e"))
      }
    }
  }

  def gettersAndSetters(tpe: Type): (List[MethodSymbol], List[MethodSymbol]) = {
    val methods = tpe.decls.toList collect {
      case sym: MethodSymbol if sym.isMethod && sym.isPublic => sym
    }

    val byName = methods.map(s => s.name.toString -> s).toMap

    val getters = methods.collect {
      case Getter(x @ (_, _)) => x
    }.filter(x => byName.exists { case (name, sym) =>
      name == "set" + x._1 &&
        sym.typeSignature.finalResultType =:= typeOf[Unit] &&
        (sym.paramLists match {
          case List(List(t)) => t.typeSignature =:= x._2.typeSignature.finalResultType
          case _ => false
        })
    })

    val setters = getters.map(x => byName("set" + x._1))
    (getters.map(_._2), setters)
  }

  def propsOf(tpe: Type): List[(TermName, Type)] = {
    val (getters, _) = gettersAndSetters(tpe)
    getters.map {
      case Getter(name, sym) => (TermName(name.head.toLower + name.tail), sym.typeSignatureIn(tpe).finalResultType)
    }
  }

  def isBean(tpe: Type): Boolean = {
    val sym = tpe.typeSymbol.asType
    !isReprType(tpe) &&
      !sym.isAbstract &&
      tpe.decls.exists(x => x.isConstructor && x.isPublic && x.typeSignature.paramLists == List(List()))
  }
}
