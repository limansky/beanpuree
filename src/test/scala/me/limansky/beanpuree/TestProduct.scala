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

case class TestProduct(count: Int, string: String, amount: java.lang.Long, enabled: Boolean)

case class TestProductDisordered(string: String, amount: java.lang.Long, count: Int, enabled: Boolean)

case class TestProductScala(count: Int, string: String, amount: Option[Long], enabled: Boolean)

case class TestProductScalaDisordered(string: Option[String], enabled: Boolean, amount: Long, count: Int)
