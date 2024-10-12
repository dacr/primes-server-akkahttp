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
package primes.routing

import org.apache.pekko.http.scaladsl.model.*
import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.server.Route
import com.github.pjfanning.pekkohttpjson4s.Json4sSupport.*
import primes.ServiceDependencies
import primes.tools.DateTimeTools

import java.util.UUID
import scala.concurrent.ExecutionContextExecutor

case class PrimesRouting(dependencies: ServiceDependencies) extends Routing with DateTimeTools {
  val apiURL = dependencies.config.primes.site.apiURL
  val meta = dependencies.config.primes.metaInfo
  val engine = dependencies.engine
  val startedDate = now()
  val instanceUUID = UUID.randomUUID().toString

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  override def routes: Route = pathPrefix("api") {
    concat(info, random)
  }

  def info: Route = {
    get {
      path("info") {
        complete(
          Map(
            "instanceUUID" -> instanceUUID,
            "startedOn" -> epochToUTCDateTime(startedDate),
            "version" -> meta.version,
            "buildDate" -> meta.buildDateTime
          )
        )
      }
    }
  }

  def random: Route = {
    get {
      path("random") {
        parameters(
          "lowerLimit".as[BigInt].optional,
          "upperLimit".as[BigInt].optional
        ) { (lowerLimit, upperLimit) =>
          onSuccess(engine.randomPrimeBetween(lowerLimit, upperLimit)) {
            case Some(value) => complete(value)
            case None => complete(StatusCodes.NotFound -> "Invalid range or no primes number found")
          }
        }
      }
    }
  }
}
