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
package primes.server.akkahttp.templating

import org.fusesource.scalate._
import primes.server.akkahttp.ServiceConfig

trait Templating {
  def layout(templateName:String, properties:Map[String,Any]):String
}

case class ScalateTemplating(config:ServiceConfig) extends Templating {
  private val engine = new TemplateEngine

  override def layout(templateName:String, properties:Map[String,Any]):String = {
    engine.layout(templateName, properties)
  }
}
