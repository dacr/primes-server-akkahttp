/*
 * Copyright 2020-2022 David Crosson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package primes.tools

import org.json4s.{DefaultFormats, Formats}
import org.json4s.ext.{JavaTimeSerializers, JavaTypesSerializers}
import org.json4s.jackson.Serialization

trait JsonImplicits {
  implicit val chosenSerialization: Serialization.type = Serialization
  implicit val chosenFormats: Formats = DefaultFormats.lossless ++ JavaTimeSerializers.all ++ JavaTypesSerializers.all

}
