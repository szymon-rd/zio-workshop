package zioworkshop.module1

import zio.{IO, Schedule, ZIO}
import zio.test.Assertion.equalTo
import zio.test.{DefaultRunnableSpec, Spec, TestFailure, TestSuccess, ZTestEnv}
import zioworkshop.Common.{OurSpec, ___}
import zio.test._
import zio.test.Assertion._
import zio.test.environment.testEnvironment

import scala.language.postfixOps

object ZioTaskExercises extends DefaultRunnableSpec {


  def spec: OurSpec = suite("ZioTaskExcercises")(
    testM("1. Equal 3") {
      for {
        a <- ZIO.effect(1)
//        b <- ___[ZIO[Any, Nothing, Int]]
        b <- ZIO.effect(2)
      } yield assert(a + b)(equalTo(3))
    },

    testM("2. Fail with FooException") {
      object FooException extends Throwable

      val task = for {
        a <- ZIO.effect(1)
//        _ <- ___[ZIO[Any, Throwable, Int]]
        _ <- ZIO.fail(FooException)
      } yield a

      assertM(task.flip)(isSubtype[FooException.type](anything))
    },

    testM("3. Recover from error") {
      object FooException extends Throwable
      val task: ZIO[Any, Throwable, Unit] = ZIO.fail(FooException)
//      val recovered: ZIO[Any, Throwable, Unit] = ___ // task.???
      val recovered: ZIO[Any, Throwable, Unit] = task.orElse(ZIO.succeed(()))
      assertM(recovered)(anything)
    },

    testM("4. Recover to 1 from FooException and to 2 for BarException") {
      object FooException extends Throwable
      object BarException extends Throwable
      val failedTaskFoo: ZIO[Any, Throwable, Int] = ZIO.fail(FooException)
      val failedTaskBar: ZIO[Any, Throwable, Int] = ZIO.fail(BarException)

//      def recoverFromErrors(zio: ZIO[Any, Throwable, Int]): ZIO[Any, Nothing, Int] = ___
      def recoverFromErrors(zio: ZIO[Any, Throwable, Int]): ZIO[Any, Nothing, Int] = zio.catchAll {
        case FooException => ZIO.succeed(1)
        case BarException => ZIO.succeed(2)
        case _ => ZIO.succeed(3)
      }

     for {
       assertFoo <- assertM(recoverFromErrors(failedTaskFoo))(equalTo(1))
       assertBar <- assertM(recoverFromErrors(failedTaskBar))(equalTo(2))
     } yield assertFoo && assertBar
    },

    testM("5. Retry action") {
      val client = HttpClient()

//      val website = client.fetch("www.google.com") // fails... Try retrying it a couple of times!
      val website = client.fetch("www.google.com").retry(Schedule.recurs(5))

      assertM(website)(containsString("google"))
    },

    testM("6. Deal with defect") {
      object FooException extends Throwable
      val client = HttpClient()

//      val io = IO.succeed(throw FooException) // A defect! Modify this line
      val io = IO.succeed(throw FooException).absorb.orElse(IO.succeed())

      assertM(io)(anything)
    },

    testM("7. Run for each element of collection") {
      val elems = (1 to 100).toList
      def plusOne(i: Int) = ZIO.succeed(i + 1)

      for {
        // Call plusOne for each of elems... But how? Look at ZIO.foreach and ZIO.collectAll
//        results <- ___[ZIO[Any, Nothing, List[Int]]]
        results <- ZIO.foreach(elems)(plusOne)
        sum = results.sum
      } yield assert(sum)(equalTo((2 to 101).sum))

    }
  ).provideLayer(testEnvironment)
}
