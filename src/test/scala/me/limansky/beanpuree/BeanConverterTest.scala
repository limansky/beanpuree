package me.limansky.beanpuree

import org.scalatest.{FlatSpec, Matchers}

class BeanConverterTest extends FlatSpec with Matchers {

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
