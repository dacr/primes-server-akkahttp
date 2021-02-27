package primes.dependencies.primesengine

trait PrimesEngine {
  def randomPrime(): BigInt

  def randomPrimeBetween(lowerLimit: BigInt, upperLimit: BigInt): BigInt

  def isPrime(value: BigInt): Boolean
}


