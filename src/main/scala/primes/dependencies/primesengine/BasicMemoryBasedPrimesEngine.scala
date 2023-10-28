package primes.dependencies.primesengine

import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import org.apache.pekko.actor.typed.scaladsl.AskPattern._
import org.apache.pekko.util.Timeout

import scala.concurrent.duration._
import fr.janalyse.primes.PrimesGenerator
import org.slf4j.LoggerFactory
import primes.PrimesConfig

import scala.concurrent.Future
import scala.util.Random


object BasicMemoryBasedPrimesEngine {
  def apply(primesConfig: PrimesConfig): PrimesEngine = new BasicMemoryBasedPrimesEngine(primesConfig)
}


class BasicMemoryBasedPrimesEngine(primesConfig: PrimesConfig) extends PrimesEngine {
  val logger = LoggerFactory.getLogger(getClass)
  val maxPrimesCount = primesConfig.behavior.maxPrimesCount
  val maxPrimesValueLimit = primesConfig.behavior.maxPrimesValueLimit
  val alreadyComputedCount = 0L

  // -----------------------------------------------------------------

  def min(x: BigInt, y: BigInt): BigInt = if (x < y) x else y
  def max(x: BigInt, y: BigInt): BigInt = if (x > y) x else y

  // -----------------------------------------------------------------

  sealed trait PrimesCommand

  case class NewPrimeComputed(value: BigInt) extends PrimesCommand

  case class RandomPrimeRequest(replyTo: ActorRef[Option[BigInt]]) extends PrimesCommand

  case class RandomPrimeBetweenRequest(lowerLimit: Option[BigInt], upperLimit: Option[BigInt], replyTo: ActorRef[Option[BigInt]]) extends PrimesCommand

  case class MaxKnownPrimesNumberRequest(replyTo: ActorRef[Option[BigInt]]) extends PrimesCommand

  case class KnownPrimesNumberCountRequest(replyTo: ActorRef[BigInt]) extends PrimesCommand

  case class CheckPrimeRequest(value: BigInt, replyTo: ActorRef[Option[Boolean]]) extends PrimesCommand

  // -----------------------------------------------------------------

  def primesBehavior(): Behavior[PrimesCommand] = {
    def updated(knownPrimes: Vector[BigInt]): Behavior[PrimesCommand] = Behaviors.receiveMessage {
      // ------------------------------
      case NewPrimeComputed(value) =>
        updated(knownPrimes :+ value)
      // ------------------------------
      case KnownPrimesNumberCountRequest(replyTo)=>
        replyTo ! BigInt(knownPrimes.size)
        Behaviors.same
      // ------------------------------
      case MaxKnownPrimesNumberRequest(replyTo) =>
        replyTo ! knownPrimes.lastOption
        Behaviors.same
      // ------------------------------
      case RandomPrimeRequest(replyTo) =>
        replyTo ! knownPrimes.lift(Random.nextInt(knownPrimes.size))
        Behaviors.same
      // ------------------------------
      case RandomPrimeBetweenRequest(lowerLimit, upperLimit, replyTo) =>
        val lowestPrime = knownPrimes.headOption.getOrElse(BigInt(0))
        val highestPrime = knownPrimes.lastOption.getOrElse(BigInt(0))
        val fromIndex = lowerLimit match {
          case None => 0
          case Some(limit) => knownPrimes.indexWhere(_ >= max(limit, lowestPrime))
        }
        val toIndex = upperLimit match {
          case None => knownPrimes.size - 1
          case Some(limit) => knownPrimes.indexWhere(_ >= min(limit, highestPrime))
        }
        val size = toIndex - fromIndex + 1
        val result = if (size <= 0) None else {
          Option(knownPrimes(fromIndex + Random.nextInt(size)))
        }
        replyTo ! result
        Behaviors.same
      // ------------------------------
      case CheckPrimeRequest(value, replyTo) =>
        val result = if (value <= knownPrimes.lastOption.getOrElse(0)) Option(knownPrimes.contains(value)) else None
        replyTo ! result
        Behaviors.same
    }

    updated(Vector.empty)
  }

  // -----------------------------------------------------------------

  implicit val primesSystem: ActorSystem[PrimesCommand] = ActorSystem(primesBehavior(), "PrimesActorSystem")
  implicit val ec = primesSystem.executionContext
  implicit val timeout: Timeout = 3.seconds

  // -----------------------------------------------------------------

  def primesComputeShouldContinue(prime:BigInt, pos:BigInt, alreadyComputedCount:BigInt):Boolean = {
    (maxPrimesCount.isEmpty || (pos + alreadyComputedCount) <= maxPrimesCount.get) &&
      (maxPrimesValueLimit.isEmpty || (prime <= maxPrimesValueLimit.get))
  }

  val primesGenerator = Future {
    logger.info(s"Start feeding with primes number up to ${maxPrimesValueLimit} max value or ${maxPrimesCount} primes count reached")
    val primesGenerator = new PrimesGenerator[Long] // Faster than BigInt
    primesGenerator
      .primes
      .map(v => BigInt(v))
      .zip(Iterator.iterate(BigInt(1))(_ + 1))
      .takeWhile{case (prime, pos) => primesComputeShouldContinue(prime, pos, alreadyComputedCount) }
      .map{case (prime, pos) => prime}
      .foreach { value => primesSystem ! NewPrimeComputed(value)}
  }

  // -----------------------------------------------------------------

  override def maxKnownPrimesNumber(): Future[Option[BigInt]] = {
    primesSystem.ask(MaxKnownPrimesNumberRequest)
  }

  override def knownPrimesNumberCount(): Future[BigInt] = {
    primesSystem.ask(KnownPrimesNumberCountRequest)
  }

  override def randomPrime(): Future[Option[BigInt]] = {
    primesSystem.ask(RandomPrimeRequest)
  }

  override def randomPrimeBetween(lowerLimit: Option[BigInt], upperLimit: Option[BigInt]): Future[Option[BigInt]] = {
    primesSystem.ask(RandomPrimeBetweenRequest(lowerLimit, upperLimit, _))
  }

  override def isPrime(value: BigInt): Future[Option[Boolean]] = {
    primesSystem.ask(CheckPrimeRequest(value, _))
  }
}
