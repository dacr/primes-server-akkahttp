package primes.routing

import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.model.MediaTypes.`text/html`
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import primes.ServiceDependencies
import primes.tools.Templating
import yamusca.imports._
import yamusca.implicits._

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

  implicit val ec = scala.concurrent.ExecutionContext.global
  implicit val pageContextConverter = ValueConverter.deriveConverter[PageContext]
  implicit val homeContextConverter = ValueConverter.deriveConverter[HomeContext]

  val templating: Templating = Templating(dependencies.config)
  val homeLayout = (context: Context) => templating.makeTemplateLayout("primes/templates/home.html")(context)

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
              val content = homeLayout(homeContext.asContext)
              val contentType = `text/html` withCharset `UTF-8`
              HttpResponse(entity = HttpEntity(contentType, content), headers = noClientCacheHeaders)
            }
          }
        }
      }
    }
  }
}
