package zioworkshop.module4

import java.time.{OffsetDateTime, ZoneOffset}

import zio.blocking.Blocking
import zio.{Schedule, ZIO, ZManaged, ZRef}
import zio.console.putStrLn
import zio.duration.durationInt
import zio.test.Assertion.{equalTo, hasSameElements}
import zio.test.{DefaultRunnableSpec, assertM}
import zio.test._
import zio.test.environment.{TestClock, TestConsole, testEnvironment}
import zioworkshop.Common.{OurSpec, ___}
import zio.Runtime
object ZioExtraExercises extends DefaultRunnableSpec {

  def spec: OurSpec = suite("ZioExtraExercises")(
    testM("1. ZManaged") {
      // TODO fill fragments with correct putStrLn to match the test results
      val managed = ZManaged.make(
        ___
      )(
        ___
      )
      val usingFoo = managed.use {
        foo => putStrLn(s"Use $foo")
      }
      Runtime.default.unsafeRun(ZIO.effect())
      // *> is .flatMap(_ => ...)
      usingFoo *> assertM(TestConsole.output)(hasSameElements(List("Acquire foo\n", "Use foo\n", "Release foo\n")))
    },

  ).provideLayer(testEnvironment)
}
