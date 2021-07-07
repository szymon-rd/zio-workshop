package zioworkshop.module4

import zio.console.putStrLn
import zio.test.Assertion.hasSameElements
import zio.test.environment.{TestConsole, testEnvironment}
import zio.test.{DefaultRunnableSpec, assertM}
import zio.{Runtime, ZIO, ZManaged}
import zioworkshop.Common.{OurSpec, ___}
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
