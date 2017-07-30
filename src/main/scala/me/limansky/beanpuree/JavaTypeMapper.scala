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

import shapeless.labelled.FieldType
import shapeless.{::, HList, Lazy}
import shapeless.labelled.field

trait JavaTypeMapper[J, S] {
  def javaToScala(j: J): S
  def scalaToJava(s: S): J
}

trait LowPriorityJavaTypeMapper {
  implicit def dummyMapper[T]: JavaTypeMapper[T, T] = JavaTypeMapper.of(identity, identity)

}

object JavaTypeMapper extends LowPriorityJavaTypeMapper {
  def apply[J, S](implicit m: JavaTypeMapper[J, S]): JavaTypeMapper[J, S] = m

  implicit val integerMapper: JavaTypeMapper[Integer, Int] = of(_.intValue, Integer.valueOf)

  implicit val longMapper: JavaTypeMapper[java.lang.Long, Long] = of(_.longValue, java.lang.Long.valueOf)

  implicit val doubleMapper: JavaTypeMapper[java.lang.Double, Double] = of(_.doubleValue, java.lang.Double.valueOf)

  implicit val bigDecimalMapper: JavaTypeMapper[java.math.BigDecimal, BigDecimal] = of(BigDecimal.apply, _.underlying)

  implicit val bigIntegerMapper: JavaTypeMapper[java.math.BigInteger, BigInt] = of(BigInt.apply, _.underlying)

  implicit val charMapper: JavaTypeMapper[Character, Char] = of(_.charValue, Character.valueOf)

  implicit def fieldMapper[K, J, S](implicit m: JavaTypeMapper[J, S]): JavaTypeMapper[FieldType[K, J], FieldType[K, S]] = of(
    j => field[K](m.javaToScala(j)),
    s => field[K](m.scalaToJava(s))
  )

  implicit def nullableToMappedOption[J >: Null, S](implicit inner: JavaTypeMapper[J, S]): JavaTypeMapper[J, Option[S]] = of(
    x => Option(x).map(inner.javaToScala),
    _.map(inner.scalaToJava).orNull
  )

  implicit def hconsMapper[JH, JT <: HList, SH, ST <: HList](implicit
    hMapper: Lazy[JavaTypeMapper[JH, SH]],
    tMapper: JavaTypeMapper[JT, ST]
  ): JavaTypeMapper[JH :: JT, SH :: ST] = of(
    j => hMapper.value.javaToScala(j.head) :: tMapper.javaToScala(j.tail),
    s => hMapper.value.scalaToJava(s.head) :: tMapper.scalaToJava(s.tail)
  )

  def of[J, S](jts: J => S, stj: S => J): JavaTypeMapper[J, S] = new JavaTypeMapper[J, S] {
    override def javaToScala(j: J): S = jts(j)
    override def scalaToJava(s: S): J = stj(s)
  }
}
