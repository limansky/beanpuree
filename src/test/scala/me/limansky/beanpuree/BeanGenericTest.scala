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
import shapeless.{::, Generic, HList, HNil}

class BeanGenericTest extends FlatSpec with Matchers {

  "BeanGeneric" should "Build Repr type" in {
    val gen = BeanGeneric[TestBean]
    implicitly[gen.Repr =:= (Int :: String :: java.lang.Long :: HNil)]
  }

  it should "convert bean to HList" ignore {

  }

  it should "convert Hlist to Bean" in {
    val gen = BeanGeneric[TestBean]

    val bean = gen.from(42 :: "abc" :: java.lang.Long.valueOf(25L) :: HNil)

    bean.getCount shouldEqual 42
    bean.getString shouldEqual "abc"
    bean.getAmount shouldEqual 25L
  }
}
