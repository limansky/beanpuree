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

import org.scalatest.{FlatSpec, Matchers}
import shapeless.Generic

class BeanConverterTest extends FlatSpec with Matchers {

  "BeanConverter" should "convert bean to case class" in {
    val converter = BeanConverter[TestBean, TestProduct]

    val bean = new TestBean
    bean.setAmount(1L)
    bean.setCount(4)
    bean.setString("text")
    bean.setEnabled(true)

    converter.beanToProduct(bean) shouldEqual TestProduct(4, "text", 1L, true)
  }

  it should "convert case class to bean" in {
    val converter = BeanConverter[TestBean, TestProduct]

    val bean = converter.productToBean(TestProduct(8, "back to Java", 43L, true))

    bean.getAmount shouldEqual 43L
    bean.getCount shouldEqual 8
    bean.getString shouldEqual "back to Java"
    bean.isEnabled shouldBe true
  }

  it should "ignore fields order" in {
    val converter = BeanConverter[TestBean, TestProductDisordered]
    val bean = converter.productToBean(TestProductDisordered("a", 5L, 4, false))

    bean.getCount shouldEqual 4
    bean.getAmount shouldEqual 5
    bean.isEnabled shouldBe false
    bean.getString shouldEqual "a"
  }
}
