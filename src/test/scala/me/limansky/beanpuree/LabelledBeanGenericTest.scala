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

import shapeless.HNil
import shapeless.syntax.singleton._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LabelledBeanGenericTest extends AnyFlatSpec with Matchers {
  "LabelledBeanGeneric" should "convert bean to Repr" in {
    val gen = LabelledBeanGeneric[TestBean]

    val bean = new TestBean
    bean.setEnabled(true)
    bean.setCount(4)
    bean.setString("451")
    bean.setAmount(5L)

    gen.to(bean) shouldEqual Symbol("count") ->> 4 :: Symbol("string") ->> "451" :: Symbol("amount") ->> java.lang.Long
      .valueOf(5) :: Symbol("enabled") ->> true :: HNil
  }

  it should "convert Repr to bean" in {
    val gen = LabelledBeanGeneric[TestBean]

    val bean = gen.from(
      Symbol("count") ->> 4 :: Symbol("string") ->> "451" :: Symbol("amount") ->> java.lang.Long.valueOf(5) :: Symbol(
        "enabled"
      ) ->> true :: HNil
    )
    bean.getAmount shouldEqual 5L
    bean.getCount shouldEqual 4
    bean.getString shouldEqual "451"
    bean.isEnabled shouldBe true
  }

  it should "ignore invalid getters and setters" in {
    val gen = LabelledBeanGeneric[WeirdBean]

    val bean = new WeirdBean
    bean.setW(5)
    bean.setX(6)
    bean.setY(7)

    gen.to(bean) shouldEqual Symbol("y") ->> 7 :: HNil
  }

  it should "be aware of public methods only" in {
    val gen = LabelledBeanGeneric[BeanWithPrivate]

    val bean = new BeanWithPrivate
    bean.setA(42)
    bean.setS("bzzz")

    val repr = gen.to(bean)
    repr shouldEqual Symbol("a") ->> 42 :: HNil

    val bean2 = gen.from(repr)

    bean2.getA shouldEqual 42
    bean2.getS shouldEqual null
  }
}
