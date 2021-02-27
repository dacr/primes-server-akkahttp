package primes.dependencies.primesengine

import scala.concurrent.Future

trait PrimesEngine {

  def knownPrimesNumberCount(): Future[BigInt]

  def maxKnownPrimesNumber():Future[Option[BigInt]]

  def randomPrime(): Future[Option[BigInt]]

  def randomPrimeBetween(lowerLimit: Option[BigInt], upperLimit: Option[BigInt]): Future[Option[BigInt]]

  def isPrime(value: BigInt): Future[Option[Boolean]]
}


