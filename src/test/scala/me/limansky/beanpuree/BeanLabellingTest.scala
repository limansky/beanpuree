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
import shapeless.HNil

class BeanLabellingTest extends FlatSpec with Matchers {

  "BeanLabelling" should "extract field names" in {
    val lab = BeanLabelling[TestBean]
    lab() shouldEqual  'count :: 'string :: 'amount :: 'enabled :: HNil
  }

  it should "skip invalid setters and getters" in {
    val lab = BeanLabelling[WeirdBean]

    lab() shouldEqual 'y :: HNil
  }
}
