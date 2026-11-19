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

import org.scalatest.matchers.*
import org.scalatest.wordspec.*

class SiteConfigTest extends AnyWordSpec with should.Matchers {
  "Site configuration" should {
    "not generate double slashes when the url ends with a slash" in {
      val site = SiteConfig(prefix = None, url = "https://primes.code-examples.org/")
      site.baseURL shouldBe "https://primes.code-examples.org"
      site.swaggerUserInterfaceURL shouldBe "https://primes.code-examples.org/swagger"
      site.swaggerURL shouldBe "https://primes.code-examples.org/swagger/swagger.json"
      site.apiURL shouldBe "https://primes.code-examples.org/api"
    }
    "not generate double slashes with a prefix" in {
      val site = SiteConfig(prefix = Some("primes/"), url = "https://example.org//")
      site.baseURL shouldBe "https://example.org/primes"
      site.swaggerUserInterfaceURL shouldBe "https://example.org/primes/swagger"
    }
  }
}
