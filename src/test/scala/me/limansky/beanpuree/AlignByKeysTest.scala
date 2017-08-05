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
import shapeless.record.Record
import shapeless.syntax.singleton._
import shapeless.{HList, HNil}

class AlignByKeysTest extends FlatSpec with Matchers {

  "AlingByName" should "align by name" in {

    type Test = Record.`'a -> String, 'b -> Int, 'c -> Double`.T

    type K1 = HList.`'a, 'b, 'c`.T
    type K2 = HList.`'b, 'a, 'c`.T
    type K3 = HList.`'c, 'a, 'b`.T

    val t = 'a ->> "test" :: 'b ->> 8 :: 'c ->> 5.5d :: HNil

    AlignByKeys[Test, K1].apply(t) shouldEqual t
    AlignByKeys[Test, K2].apply(t) shouldEqual 'b ->> 8 :: 'a ->> "test" :: 'c ->> 5.5d :: HNil
    AlignByKeys[Test, K3].apply(t) shouldEqual 'c ->> 5.5d :: 'a ->> "test" :: 'b ->> 8 :: HNil
  }

}
