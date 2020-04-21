/*
 * Copyright 2020 David Crosson
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
package primes.server.akkahttp.routing

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.CacheDirectives.{`max-age`, `must-revalidate`, `no-cache`, `no-store`}
import akka.http.scaladsl.model.headers.`Cache-Control`
import org.json4s.{DefaultFormats, Formats, native}
import org.json4s.native.Serialization
import org.json4s.ext.{JavaTimeSerializers, JavaTypesSerializers}

trait Routing {
  def routes: Route

  implicit val chosenSerialization: Serialization.type = native.Serialization
  implicit val chosenFormats: Formats = DefaultFormats.lossless ++ JavaTimeSerializers.all ++ JavaTypesSerializers.all
  val noClientCacheHeaders: List[HttpHeader] = List(`Cache-Control`(`no-cache`, `no-store`, `max-age`(0), `must-revalidate`))
  val clientCacheHeaders: List[HttpHeader] = List(`Cache-Control`(`max-age`(86400)))
}
