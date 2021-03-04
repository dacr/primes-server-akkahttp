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
package primes

import primes.dependencies.primesengine.{BasicFileBasedPrimesEngine, BasicMemoryBasedPrimesEngine, PrimesEngine}

trait ServiceDependencies {
  val config:ServiceConfig
  val engine:PrimesEngine
}

object ServiceDependencies {
  def defaults:ServiceDependencies = {
    val chosenConfig = ServiceConfig()
    new ServiceDependencies {
      override val config: ServiceConfig = chosenConfig
      override val engine: PrimesEngine = BasicFileBasedPrimesEngine(chosenConfig.primes)
    }
  }
  def testDefaults:ServiceDependencies = {
    val chosenConfig = ServiceConfig()
    new ServiceDependencies {
      override val config: ServiceConfig = chosenConfig
      override val engine: PrimesEngine = BasicMemoryBasedPrimesEngine(chosenConfig.primes)
    }
  }
}