package me.limansky.beanpuree

import org.scalatest.{FlatSpec, Matchers}
import shapeless.{::, HList, HNil}

class BeanGenericTest extends FlatSpec with Matchers {

  "BeanGeneric" should "Build Repr type" in {
    val gen = BeanGeneric[TestBean]
    //FIX ME
    // implicitly[gen.Repr =:= (Int :: String :: Long :: HNil)]
  }
}
