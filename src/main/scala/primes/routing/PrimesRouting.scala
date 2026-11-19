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
package primes.routing

import primes.ServiceDependencies
import primes.api.{ApiEndpoints, ApiError, ServiceInfo}
import primes.tools.DateTimeTools

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

case class PrimesRouting(dependencies: ServiceDependencies) extends Routing with DateTimeTools {
  private given ExecutionContext = ExecutionContext.global

  private val meta   = dependencies.config.primes.metaInfo
  private val engine = dependencies.engine

  private val serviceInfo = ServiceInfo(
    instanceUUID = UUID.randomUUID(),
    startedOn = epochToUTCDateTime(now()),
    version = meta.version,
    buildDate = meta.buildDateTime
  )

  private val notFound = ApiError("Invalid range or no primes number found")

  private val info = ApiEndpoints.info.serverLogicSuccess[Future](_ => Future.successful(serviceInfo))

  private val random = ApiEndpoints.random.serverLogic[Future] { (lowerLimit, upperLimit) =>
    engine.randomPrimeBetween(lowerLimit, upperLimit).map(_.toRight(notFound))
  }

  override def endpoints = List(info, random)
}
