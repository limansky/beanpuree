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

import java.math.{BigInteger, BigDecimal => JavaBigDecimal}

import org.scalatest.{FlatSpec, Matchers}
import shapeless.{::, HNil, LabelledGeneric}

class JavaTypeMapperTest extends FlatSpec with Matchers {

  "Mappers" should "support mapping between Java Integer and Int" in {
    val m = JavaTypeMapper[Integer, Int]

    m.javaToScala(Integer.valueOf(42)) shouldEqual 42
    m.scalaToJava(25) shouldEqual Integer.valueOf(25)
  }

  it should "support mapping between Java Double and Double" in {
    val m = JavaTypeMapper[java.lang.Double, Double]

    m.javaToScala(java.lang.Double.valueOf(42)) shouldEqual 42d
    m.scalaToJava(25d) shouldEqual java.lang.Double.valueOf(25)
  }

  it should "support mapping between Java Long and Long" in {
    val m = JavaTypeMapper[java.lang.Long, Long]

    m.javaToScala(java.lang.Long.valueOf(42)) shouldEqual 42l
    m.scalaToJava(25l) shouldEqual java.lang.Long.valueOf(25)
  }

  it should "support mapping between Character and Char" in {
    val m = JavaTypeMapper[Character, Char]

    m.javaToScala(Character.valueOf('a')) shouldEqual 'a'
    m.scalaToJava('b') shouldEqual Character.valueOf('b')
  }

  it should "support mapping between Java BigDecimal and BigDecimal" in {
    val m = JavaTypeMapper[JavaBigDecimal, BigDecimal]

    m.javaToScala(JavaBigDecimal.valueOf(42)) shouldEqual BigDecimal(42)
    m.scalaToJava(1234) shouldEqual JavaBigDecimal.valueOf(1234)
  }

  it should "support mapping between BigInteger and BigInt" in {
    val m = JavaTypeMapper[BigInteger, BigInt]

    m.javaToScala(BigInteger.valueOf(223322)) shouldEqual BigInt(223322)
    m.scalaToJava(4321) shouldEqual BigInteger.valueOf(4321)
  }

  it should "throw NPE if Java number class is null" in {
    val m = JavaTypeMapper[Integer, Int]
    an[NullPointerException] should be thrownBy m.javaToScala(null)
  }

  it should "throw NPE if Java BigDecimal is null" in {
    val m = JavaTypeMapper[java.math.BigDecimal, BigDecimal]
    an[NullPointerException] should be thrownBy m.scalaToJava(null)

  }

  it should "support mapping T to Option[T]" in {
    val m = JavaTypeMapper[String, Option[String]]

    m.javaToScala("hello") shouldEqual Some("hello")
    m.scalaToJava(Some("world")) shouldEqual "world"

    m.javaToScala(null) shouldEqual None
    m.scalaToJava(None) shouldBe null
  }

  it should "support mapping T to Option[U]" in {
    val m = JavaTypeMapper[Integer, Option[Int]]

    m.javaToScala(Integer.valueOf(12345)) shouldEqual Some(12345)
    m.javaToScala(null) shouldEqual None

    m.scalaToJava(Some(54321)) shouldEqual 54321
    m.scalaToJava(None) shouldBe null
  }

  it should "support mapping HLists" in {
    val m = JavaTypeMapper[Integer :: String :: java.lang.Double :: HNil, Int :: String :: Option[Double] :: HNil]

    m.javaToScala(Integer.valueOf(42) :: "test me" :: java.lang.Double.valueOf(33d) :: HNil) shouldEqual 42 :: "test me" :: Some(33d) :: HNil
    m.javaToScala(Integer.valueOf(142) :: null :: null :: HNil) shouldEqual 142 :: null :: None :: HNil

    m.scalaToJava(212 :: "abc" :: Some(13.33) :: HNil) shouldEqual Integer.valueOf(212) :: "abc" :: 13.33 :: HNil
    m.scalaToJava(222 :: null :: None :: HNil) shouldEqual Integer.valueOf(222) :: null :: null :: HNil
  }

  it should "support mapping labelled HLists" in {
    import shapeless.record._
    import shapeless.syntax.singleton._

    type JavaHList = Record.`'x -> String, 'y -> Integer`.T
    type ScalaHList = Record.`'x -> String, 'y -> Int`.T

    val m = JavaTypeMapper[JavaHList, ScalaHList]

    m.javaToScala(('x ->> "hello") :: ('y ->> Integer.valueOf(123)) :: HNil) shouldEqual ('x ->> "hello") :: ('y ->> 123) :: HNil
    m.scalaToJava(('x ->> "world") :: ('y ->> 42) :: HNil) shouldEqual ('x ->> "world") :: ('y ->> Integer.valueOf(42)) :: HNil
  }
}
