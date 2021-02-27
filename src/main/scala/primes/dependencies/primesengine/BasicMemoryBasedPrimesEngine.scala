package primes.dependencies.primesengine

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import scala.concurrent.duration._

import fr.janalyse.primes.PrimesGenerator
import primes.PrimesConfig

import scala.concurrent.Future
import scala.util.Random


object BasicMemoryBasedPrimesEngine {
  def apply(primesConfig: PrimesConfig): PrimesEngine = new BasicMemoryBasedPrimesEngine(primesConfig)
}


class BasicMemoryBasedPrimesEngine(primesConfig: PrimesConfig) extends PrimesEngine {

  val maxUpperLimit = 5_000_000

  // -----------------------------------------------------------------

  def min(x: BigInt, y: BigInt): BigInt = if (x < y) x else y

  // -----------------------------------------------------------------

  sealed trait PrimesCommand

  case class NewPrimeComputed(value: BigInt) extends PrimesCommand

  case class RandomPrimeRequest(replyTo: ActorRef[Option[BigInt]]) extends PrimesCommand

  case class RandomPrimeBetweenRequest(lowerLimit: BigInt, upperLimit: BigInt, replyTo: ActorRef[Option[BigInt]]) extends PrimesCommand

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
        val fromIndex = knownPrimes.indexWhere(_ >= min(lowerLimit, maxUpperLimit))
        val toIndex = knownPrimes.indexWhere(_ > min(upperLimit, maxUpperLimit))
        val size = toIndex - fromIndex
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

  val primesGenerator = Future {
    val ngen = new PrimesGenerator[Long] // Faster than BigInt
    ngen.primes.take(maxUpperLimit).map(v => BigInt(v)).foreach { value =>
      primesSystem ! NewPrimeComputed(value)
    }
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

  override def randomPrimeBetween(lowerLimit: BigInt, upperLimit: BigInt): Future[Option[BigInt]] = {
    primesSystem.ask(RandomPrimeBetweenRequest(lowerLimit, upperLimit, _))
  }

  override def isPrime(value: BigInt): Future[Option[Boolean]] = {
    primesSystem.ask(CheckPrimeRequest(value, _))
  }
}
