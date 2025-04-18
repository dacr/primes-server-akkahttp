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

import org.apache.pekko.http.scaladsl.model.HttpCharsets._
import org.apache.pekko.http.scaladsl.model.{HttpEntity, HttpResponse}
import org.apache.pekko.http.scaladsl.model.MediaTypes.{`application/json`, `text/html`}
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route
import primes.ServiceDependencies
import primes.templates.txt.SwaggerJson
import primes.templates.html.SwaggerUI

case class SwaggerRouting(dependencies: ServiceDependencies) extends Routing {
  val pageContext        = PageContext(dependencies.config.primes)
  val swaggerJsonContent = SwaggerJson.render(pageContext).toString
  val swaggerUIContent   = SwaggerUI.render(pageContext).toString

  def swaggerSpec: Route = path("swagger.json") {
    val contentType = `application/json`
    complete {
      HttpResponse(entity = HttpEntity(contentType, swaggerJsonContent), headers = noClientCacheHeaders)
    }
  }

  def swaggerUI: Route =
    pathEndOrSingleSlash {
      get {
        val contentType = `text/html` withCharset `UTF-8`
        complete {
          HttpResponse(entity = HttpEntity(contentType, swaggerUIContent), headers = noClientCacheHeaders)
        }
      }
    }

  override def routes: Route = pathPrefix("swagger") { concat(swaggerUI, swaggerSpec) }
}
