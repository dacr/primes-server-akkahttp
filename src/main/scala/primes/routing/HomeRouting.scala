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
import primes.templates.html.HomeTemplate
import sttp.tapir.*

import scala.concurrent.{ExecutionContext, Future}

case class HomeContext(
  context: PageContext,
  randomPrimeNumber: String,
  computedCount: String,
  maxKnown: String
)

case class HomeRouting(dependencies: ServiceDependencies) extends Routing {
  private given ExecutionContext = ExecutionContext.global

  private val pageContext = PageContext(dependencies.config.primes)
  private val engine      = dependencies.engine
  private val noneYet     = "no already computed prime number available"

  private val home =
    endpoint.get
      .in("") // root path only, an endpoint without any path input matches all paths
      .out(htmlBodyUtf8)
      .out(header(Routing.noClientCacheHeader))
      .serverLogicSuccess[Future] { _ =>
        for {
          maxKnown      <- engine.maxKnownPrimesNumber()
          computedCount <- engine.knownPrimesNumberCount()
          primeNumber   <- engine.randomPrime()
        } yield {
          val homeContext = HomeContext(
            context = pageContext,
            randomPrimeNumber = primeNumber.map(_.toString).getOrElse(noneYet),
            computedCount = computedCount.toString,
            maxKnown = maxKnown.map(_.toString).getOrElse(noneYet)
          )
          HomeTemplate.render(homeContext).toString
        }
      }

  override def endpoints = List(home)
}
