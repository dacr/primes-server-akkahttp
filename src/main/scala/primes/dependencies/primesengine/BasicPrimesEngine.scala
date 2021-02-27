package primes.dependencies.primesengine

import primes.PrimesConfig

object BasicPrimesEngine {
  def apply(primesConfig:PrimesConfig):PrimesEngine = new BasicPrimesEngine(primesConfig)
}


class BasicPrimesEngine(primesConfig:PrimesConfig) extends PrimesEngine {

  def randomPrime(): BigInt = {
    17
  }

  override def randomPrimeBetween(lowerLimit: BigInt, upperLimit: BigInt): BigInt = {
    17
  }

  override def isPrime(value: BigInt): Boolean = {
    true
  }
}
