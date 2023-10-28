package primes.dependencies.primesengine

import org.apache.pekko.actor.typed.scaladsl.AskPattern._
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import org.apache.pekko.util.Timeout
import fr.janalyse.primes.PrimesGenerator
import org.slf4j.LoggerFactory
import primes.PrimesConfig

import java.io.{File, IOException, RandomAccessFile}
import java.nio.{ByteBuffer, LongBuffer}
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success}

object BasicFileBasedPrimesEngine {
  def apply(primesConfig: PrimesConfig): PrimesEngine = new BasicFileBasedPrimesEngine(primesConfig)
}


class BasicFileBasedPrimesEngine(primesConfig: PrimesConfig) extends PrimesEngine {
  val logger = LoggerFactory.getLogger(getClass)
  val maxPrimesCount = primesConfig.behavior.maxPrimesCount
  val maxPrimesValueLimit = primesConfig.behavior.maxPrimesValueLimit

  // -----------------------------------------------------------------
  private val storageConfig = primesConfig.behavior.fileSystemStorage
  private val storageDirectory = {
    val path = new File(storageConfig.path)
    if (!path.exists()) {
      logger.info(s"Creating storage base directory : $path")
      if (path.mkdirs()) logger.info(s"Storage base directory $path created")
      else {
        val message = s"Unable to create storage base directory $path"
        logger.error(message)
        throw new RuntimeException(message)
      }
    }
    logger.info(s"Using $path as storage file system")
    path
  }
  private val dataStoreFile = {
    val file = new File(storageDirectory, "primes-long.data")
    if (!file.exists()) {
      logger.info(s"Creating empty data store file : $file")
      if (file.createNewFile()) logger.info(s"Data store file $file created")
      else {
        val message = s"Unable to create data store file $file"
        logger.error(message)
        throw new RuntimeException(message)
      }
    }
    file
  }
  // -----------------------------------------------------------------

  def min(x: BigInt, y: BigInt): BigInt = if (x < y) x else y

  def max(x: BigInt, y: BigInt): BigInt = if (x > y) x else y

  // -----------------------------------------------------------------

  sealed trait PrimesCommand

  object StartBackgroundCompute extends PrimesCommand

  case class NewPrimeComputed(value: BigInt) extends PrimesCommand

  case class RandomPrimeRequest(replyTo: ActorRef[Option[BigInt]]) extends PrimesCommand

  case class RandomPrimeBetweenRequest(lowerLimit: Option[BigInt], upperLimit: Option[BigInt], replyTo: ActorRef[Option[BigInt]]) extends PrimesCommand

  case class MaxKnownPrimesNumberRequest(replyTo: ActorRef[Option[BigInt]]) extends PrimesCommand

  case class KnownPrimesNumberCountRequest(replyTo: ActorRef[BigInt]) extends PrimesCommand

  case class CheckPrimeRequest(value: BigInt, replyTo: ActorRef[Option[Boolean]]) extends PrimesCommand


  // -----------------------------------------------------------------

  def primesBehavior(): Behavior[PrimesCommand] = {
    val readAccessFile: RandomAccessFile = new RandomAccessFile(dataStoreFile, "r")
    val initialFileSize = readAccessFile.length()
    val initialPrimesCount = (initialFileSize / 8)
    val initialLatestKnownPrime = initialPrimesCount match {
      case 0 => None
      case count =>
        readAccessFile.seek((count - 1) * 8)
        Some(BigInt(readAccessFile.readLong()))
    }
    val writeAccessFile: RandomAccessFile = new RandomAccessFile(dataStoreFile, "rw")
    writeAccessFile.seek(initialPrimesCount * 8)

    def updated(primesCount: BigInt, latestKnownPrime: Option[BigInt], backgroundCompute: Option[Future[Unit]]): Behavior[PrimesCommand] = Behaviors.setup { context =>
      implicit val ec = context.executionContext
      Behaviors.receiveMessage {
        // ------------------------------
        case StartBackgroundCompute if backgroundCompute.isEmpty =>
          val startValue: BigInt = latestKnownPrime.getOrElse(1)
          logger.info(s"Start or resume primes number compute from $startValue")
          val future = primesGeneratorIterator(context.self, startValue, primesCount)
          future.onComplete {
            case Success(_) =>
              logger.info(s"Background primes compute has finished")
            case Failure(th) =>
              logger.error(s"Background primes compute has failed", th)
          }
          updated(primesCount, latestKnownPrime, Some(future))
        case StartBackgroundCompute =>
          logger.info("Background primes compute is running or has finished")
          Behaviors.same
        // ------------------------------
        case NewPrimeComputed(value) =>
          writeAccessFile.writeLong(value.toLong)
          updated(primesCount + 1, Some(value), backgroundCompute)
        // ------------------------------
        case KnownPrimesNumberCountRequest(replyTo) =>
          replyTo ! primesCount
          Behaviors.same
        // ------------------------------
        case MaxKnownPrimesNumberRequest(replyTo) =>
          replyTo ! latestKnownPrime
          Behaviors.same
        // ------------------------------
        case RandomPrimeRequest(replyTo) =>
          if (primesCount == 0) {
            replyTo ! None
            Behaviors.same
          } else {
            val primePos = Random.nextLong(primesCount.toLong)
            readAccessFile.seek(primePos * 8)
            val randomPrime = readAccessFile.readLong()
            replyTo ! Some(randomPrime)
            Behaviors.same
          }
        // ------------------------------
        case RandomPrimeBetweenRequest(lowerLimit, upperLimit, replyTo) =>
          if (primesCount == 0) {
            replyTo ! None
            Behaviors.same
          } else {
            val fromIndex = lowerLimit match {
              case None => 0L
              case Some(limit) => nearestIndex(readAccessFile, limit.toLong, 0, primesCount.toLong - 1)
            }
            val toIndex = upperLimit match {
              case None => primesCount.toLong - 1
              case Some(limit) => nearestIndex(readAccessFile, limit.toLong, 0, primesCount.toLong - 1 )
            }
            val size = toIndex - fromIndex + 1
            val result = if (size <= 0) None else {
              val position = (fromIndex + Random.nextLong(size)) * 8
              try {
                readAccessFile.seek(position)
                Some(BigInt(readAccessFile.readLong()))
              } catch {
                case ex: IOException =>
                  logger.warn(s"Something wrong has happened while seeking or reading position=$position fromIndex=$fromIndex toIndex=$toIndex", ex)
                  None
              }
            }
            replyTo ! result
            Behaviors.same
          }
        // ------------------------------
        case CheckPrimeRequest(value, replyTo) if latestKnownPrime.isEmpty || value > latestKnownPrime.get || value < 2 =>
          replyTo ! None
          Behaviors.same
        case CheckPrimeRequest(value, replyTo) =>
          val result = searchValue(readAccessFile, value.toLong, 0, initialPrimesCount.toLong - 1)
          replyTo ! Some(result)
          Behaviors.same
      }
    }

    Behaviors.withTimers[PrimesCommand] { timer =>
      timer.startSingleTimer(StartBackgroundCompute, 1.seconds)
      updated(initialPrimesCount, initialLatestKnownPrime, None)
    }
  }

  // -----------------------------------------------------------------
  def nearestIndex(access: RandomAccessFile, value: Long, leftIndex: Long, rightIndex: Long): Long = {
    if (leftIndex > rightIndex) rightIndex else {
      val rightValue = {
        access.seek(rightIndex*8)
        access.readLong()
      }
      val leftValue = {
        access.seek(leftIndex*8)
        access.readLong()
      }
      val middleOffset = leftIndex + (rightIndex - leftIndex) / 2
      val middleValue = {
        access.seek(middleOffset*8)
        access.readLong()
      }
      if (value == middleValue) middleOffset
      else if (value < leftValue) leftIndex
      else if (value > rightValue) rightIndex
      else if (middleValue > value) nearestIndex(access, value, leftIndex, middleOffset - 1)
      else nearestIndex(access, value, middleOffset + 1, rightIndex)
    }
  }


  // -----------------------------------------------------------------
  // binary search (recherche dichotomique)

  def searchValue(access: RandomAccessFile, value: Long, leftIndex: Long, rightIndex: Long): Boolean = {
    if (leftIndex > rightIndex) false else {
      val middleIndex = leftIndex + (rightIndex - leftIndex) / 2
      val middleValue = {
        access.seek(middleIndex*8)
        access.readLong()
      }
      if (middleValue == value) true
      else if (middleValue > value) searchValue(access, value, leftIndex, middleIndex-1)
      else searchValue(access, value, middleIndex+1, rightIndex)
    }
  }


  // -----------------------------------------------------------------

  implicit val primesSystem: ActorSystem[PrimesCommand] = ActorSystem(primesBehavior(), "PrimesActorSystem")
  implicit val ec = primesSystem.executionContext
  implicit val timeout: Timeout = 3.seconds

  // -----------------------------------------------------------------

  def primesComputeShouldContinue(prime: BigInt, pos: BigInt, alreadyComputedCount: BigInt): Boolean = {
    (maxPrimesCount.isEmpty || (pos + alreadyComputedCount) <= maxPrimesCount.get) &&
      (maxPrimesValueLimit.isEmpty || (prime <= maxPrimesValueLimit.get))
  }

  def primesGeneratorIterator(receivedActor: ActorRef[NewPrimeComputed], startAfterThatValue: BigInt, alreadyComputedCount: BigInt): Future[Unit] = Future {
    logger.info(s"Start feeding with primes number up to ${maxPrimesValueLimit} max value or ${maxPrimesCount} primes count reached")
    val primesGenerator = new PrimesGenerator[Long] // Faster than BigInt
    primesGenerator
      .primesAfter(startAfterThatValue.toLong)
      .map(v => BigInt(v))
      .zip(Iterator.iterate(BigInt(1))(_ + 1))
      .takeWhile { case (prime, pos) => primesComputeShouldContinue(prime, pos, alreadyComputedCount) }
      .map { case (prime, _) => prime }
      .foreach { value => receivedActor ! NewPrimeComputed(value) }
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
