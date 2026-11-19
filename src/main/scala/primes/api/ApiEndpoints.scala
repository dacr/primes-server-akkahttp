/*
 * Copyright 2020-2026 David Crosson
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
package primes.api

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.jsoniter.*

/** Documented API endpoints definitions, the OpenAPI specification is generated from them. Server logic is provided separately, see the routing package.
  */
object ApiEndpoints {

  val description: String =
    """Primes number service, a large set of primes number is already computed and ready for any query.
      |
      |Primes are computed in the background and stored, queries only use the already computed ones.""".stripMargin

  object Tags {
    val primes  = "primes"
    val service = "service"

    val descriptions: List[(String, String)] = List(
      primes  -> "Primes number queries, using already computed primes",
      service -> "Service information and health"
    )
  }

  given bigIntCodec: JsonValueCodec[BigInt] = JsonCodecMaker.make

  private val serviceEndpoint = endpoint.tag(Tags.service)
  private val primesEndpoint  = endpoint.tag(Tags.primes)

  // -------------------------------------------------------------------------------------------------------------------
  // service

  val health: PublicEndpoint[Unit, Unit, Health, Any] =
    serviceEndpoint
      .name("health")
      .summary("Service health check")
      .get
      .in("health")
      .out(jsonBody[Health].description("service health status"))

  val info: PublicEndpoint[Unit, Unit, ServiceInfo, Any] =
    serviceEndpoint
      .name("info")
      .summary("General information about the service")
      .get
      .in("api" / "info")
      .out(jsonBody[ServiceInfo].description("service information"))

  // -------------------------------------------------------------------------------------------------------------------
  // primes

  val random: PublicEndpoint[(Option[BigInt], Option[BigInt]), ApiError, BigInt, Any] =
    primesEndpoint
      .name("random")
      .summary("Get a random prime number among those already computed")
      .get
      .in("api" / "random")
      .in(query[Option[BigInt]]("lowerLimit").description("random prime numbers range lower limit").example(Some(BigInt(1000))))
      .in(query[Option[BigInt]]("upperLimit").description("random prime numbers range upper limit").example(Some(BigInt(2000))))
      .out(jsonBody[BigInt].description("a randomly chosen prime number").example(BigInt(1009)))
      .errorOut(
        statusCode(StatusCode.NotFound)
          .description("no primes number available yet, or the given range is beyond the computed primes")
          .and(jsonBody[ApiError])
      )

  val all: List[AnyEndpoint] = List(
    random,
    info,
    health
  )
}
