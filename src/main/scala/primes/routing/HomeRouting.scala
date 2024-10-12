package primes.routing

import org.apache.pekko.http.scaladsl.model.HttpCharsets.*
import org.apache.pekko.http.scaladsl.model.{HttpEntity, HttpResponse}
import org.apache.pekko.http.scaladsl.model.MediaTypes.`text/html`
import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.server.Route
import primes.ServiceDependencies
import primes.templates.html.HomeTemplate

import scala.concurrent.ExecutionContextExecutor

case class HomeContext(
  context: PageContext,
  randomPrimeNumber: String,
  computedCount: String,
  maxKnown: String
)

case class HomeRouting(dependencies: ServiceDependencies) extends Routing {
  override def routes: Route = home

  val site = dependencies.config.primes.site
  val pageContext = PageContext(dependencies.config.primes)

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  def home: Route = pathEndOrSingleSlash {
    get {
      complete {
        dependencies.engine.maxKnownPrimesNumber().map { maxKnown =>
          dependencies.engine.knownPrimesNumberCount().map { computedCount =>
            dependencies.engine.randomPrime().map { primeNumber =>
              val homeContext = HomeContext(
                context = pageContext,
                randomPrimeNumber = primeNumber.map(_.toString).getOrElse("no already computed prime number available"),
                computedCount = computedCount.toString,
                maxKnown = maxKnown.map(_.toString).getOrElse("no already computed prime number available"),
              )
              val content = HomeTemplate.render(homeContext).toString()
              val contentType = `text/html` withCharset `UTF-8`
              HttpResponse(entity = HttpEntity(contentType, content), headers = noClientCacheHeaders)
            }
          }
        }
      }
    }
  }
}
