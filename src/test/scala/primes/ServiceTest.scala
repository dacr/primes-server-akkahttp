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
package primes

import com.github.plokhotnyuk.jsoniter_scala.core.readFromString
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.*
import org.scalatest.wordspec.*
import primes.api.{ApiError, Health, ServiceInfo}

class ServiceTest extends AnyWordSpec with should.Matchers with ScalatestRouteTest {

  val routes = ServiceRoutes(ServiceDependencies.testDefaults).routes

  "Primes Service" should {
    "Respond OK when pinged" in {
      Get("/health") ~> routes ~> check {
        readFromString[Health](responseAs[String]) shouldBe Health(true, "alive")
      }
    }
    "Be able to return a static asset" in {
      Get("/txt/LICENSE-2.0.txt") ~> routes ~> check {
        responseAs[String] should include regex "Apache License"
      }
      Get("/txt/TERMS-OF-SERVICE.txt") ~> routes ~> check {
        responseAs[String] should include regex "WARRANTY"
      }
    }
    "Be able to return an embedded webjar asset" in {
      Get("/assets/jquery/jquery.js") ~> routes ~> check {
        responseAs[String] should include regex "jQuery JavaScript Library"
      }
    }
    "Respond a primes related home page content" in {
      info("The first content page can be slow because of templates runtime compilation")
      Get() ~> routes ~> check {
        responseAs[String] should include regex "Primes"
      }
    }
    "Respond to info request" in {
      Get("/api/info") ~> routes ~> check {
        readFromString[ServiceInfo](responseAs[String]).version should not be empty
      }
    }
    "Respond to random request" in {
      Get("/api/random?lowerLimit=2&upperLimit=1000") ~> routes ~> check {
        status match {
          case StatusCodes.OK       => BigInt(responseAs[String].trim) should (be >= BigInt(2) and be <= BigInt(1000))
          case StatusCodes.NotFound => readFromString[ApiError](responseAs[String]).message should not be empty
          case other                => fail(s"unexpected status $other")
        }
      }
    }
    "Reject invalid random request parameters" in {
      Get("/api/random?lowerLimit=abc") ~> routes ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }
    "Serve the generated API specification" in {
      Get("/swagger/swagger.json") ~> routes ~> check {
        val spec = responseAs[String]
        spec should include("/api/random")
        spec should include("/api/info")
        spec should include("lowerLimit")
      }
      Get("/swagger/docs.yaml") ~> routes ~> check {
        responseAs[String] should include("openapi")
      }
    }
  }
}
