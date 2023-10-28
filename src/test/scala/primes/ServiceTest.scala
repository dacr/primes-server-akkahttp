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
package primes

import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.wordspec._
import org.scalatest.matchers._
import org.scalatest.OptionValues._
import org.json4s.{DefaultFormats, Formats}
import org.json4s.ext.{JavaTimeSerializers, JavaTypesSerializers}
import org.json4s.jackson.Serialization
import primes.{ServiceDependencies, ServiceRoutes}
import primes.routing.Health
import primes.tools.JsonImplicits


class ServiceTest extends AnyWordSpec with should.Matchers with ScalatestRouteTest with JsonImplicits {

  val routes = ServiceRoutes(ServiceDependencies.testDefaults).routes

  "Primes Service" should  {
    "Respond OK when pinged" in {
      Get("/health") ~> routes ~> check {
        import com.github.pjfanning.pekkohttpjson4s.Json4sSupport._
        responseAs[Health] shouldBe Health(true,"alive")
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
  }
}

