package zioworkshop.module5

import zio.console.putStrLn
import zio.test.Assertion.{equalTo, _}
import zio.test.environment.{TestConsole, testEnvironment}
import zio.test.{DefaultRunnableSpec, _}
import zio.{Fiber, FiberRef, Has, IO, Ref, ZIO, ZLayer, ZManaged}
import zioworkshop.Common.OurSpec


object ZioExtraExercises extends DefaultRunnableSpec {

  def spec: OurSpec = suite("ZioExtraExercises")(
    testM("1. ZManaged") {
      val managed = ZManaged.make(
        putStrLn("Acquire foo").as("foo")
      )(
        foo => putStrLn(s"Release $foo").orDie
      )
      val usingFoo = managed.use {
        foo => putStrLn(s"Use $foo")
      }
      // *> is .flatMap(_ => ...)
      usingFoo *> assertM(TestConsole.output)(hasSameElements(List("Acquire foo\n", "Use foo\n", "Release foo\n")))
    }

  ).provideLayer(testEnvironment)
}
