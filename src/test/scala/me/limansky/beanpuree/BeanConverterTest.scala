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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BeanConverterTest extends AnyFlatSpec with Matchers {

  "BeanConverter" should "convert bean to case class" in {
    val converter = BeanConverter[TestBean, TestProductScala]

    val bean = new TestBean
    bean.setAmount(1L)
    bean.setCount(4)
    bean.setString("text")
    bean.setEnabled(true)

    converter.beanToProduct(bean) shouldEqual TestProductScala(4, "text", Some(1L), true)
  }

  it should "convert case class to bean" in {
    val converter = BeanConverter[TestBean, TestProductScala]

    val bean = converter.productToBean(TestProductScala(8, "back to Java", None, true))

    bean.getAmount shouldBe null
    bean.getCount shouldEqual 8
    bean.getString shouldEqual "back to Java"
    bean.isEnabled shouldBe true
  }

  it should "ignore fields order" in {
    val converter = BeanConverter[TestBean, TestProductScalaDisordered]

    val value = TestProductScalaDisordered(Some("ignore order"), false, 42L, 12)
    val bean = converter.productToBean(value)

    bean.getAmount shouldBe 42L
    bean.getCount shouldEqual 12
    bean.getString shouldEqual "ignore order"
    bean.isEnabled shouldBe false

    converter.beanToProduct(bean) shouldEqual value
  }
}
