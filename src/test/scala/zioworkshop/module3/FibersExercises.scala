package zioworkshop.module3

import zio.test.Assertion.{equalTo, _}
import zio.test.{DefaultRunnableSpec, _}
import zio.{Fiber, FiberRef, Has, IO, Ref, ZIO, ZLayer}
import zioworkshop.Common.OurSpec

object FibersExercises extends DefaultRunnableSpec {

  def spec: OurSpec = suite("FibersExercises")(
    testM("1. Run two fibers & join them") {
      def sumRange(a: Int, b: Int): ZIO[Any, Throwable, Int] = ZIO.effect((a to b).sum)

      for {
        fiberA: Fiber.Runtime[Throwable, Int] <- sumRange(1, 10).fork
        fiberB: Fiber.Runtime[Throwable, Int] <- sumRange(11, 20).fork
//        a <- ___[ZIO[Any, Throwable, Int]] // join the two fibers
//        b <- ___[ZIO[Any, Throwable, Int]]
        a <- fiberA.join
        b <- fiberB.join
      } yield assert(a + b)(equalTo((0 to 20).sum))
    },

    testM("2. Race two fibers") {
      val zioOne: ZIO[Any, Throwable, String] = IO.effect { Thread.sleep(500); "one" }
      val zioTwo: ZIO[Any, Throwable, String] = IO.effect { "two" }
//      val faster = ___[ZIO[Any, Throwable, String]] // Use the race method on zio
      val faster = zioOne race zioTwo
      assertM(faster)(equalTo("two"))
    },

    testM("3. Collect in parallel") {
      def sumRange(range: (Int, Int)): ZIO[Any, Throwable, Int] = ZIO.effect((range._1 to range._2).sum)
      val ranges = List((1, 10), (11, 20), (21, 30), (31, 40), (41, 50))
      for {
//        sums <- ___[ZIO[Any, Throwable, Seq[Int]]] // Use foreachPar to perform sumRange for each range in parallel
        sums <- ZIO.foreachPar(ranges)(sumRange)
        allSum = sums.sum
      } yield assert(allSum)(equalTo((1 to 50).sum))
    },

    testM("4. Recover from a failing fiber") {
      object FooException extends Throwable
      def sumRange(a: Int, b: Int): ZIO[Any, Throwable, Int] = ZIO.effect((a to b).sum)

      for {
        fiberA: Fiber.Runtime[Throwable, Int] <- sumRange(1, 10).fork
        fiberB: Fiber.Runtime[Throwable, Int] <- IO.fail(FooException).fork
        //        a <- ___[ZIO[Any, Throwable, Int]] // recover to 0 from failing fiber, it's exactly like we did in module 1
        //        b <- ___[ZIO[Any, Throwable, Int]]
        a <- fiberA.join.orElse(ZIO.succeed(0))
        b <- fiberB.join.orElse(ZIO.succeed(0))
      } yield assert(a + b)(equalTo((1 to 10).sum))
    },

    testM("5. Modify a Ref") {
      for {
        ref <- Ref.make(0)
//        _ <-  ___[ZIO[Any, Nothing, Unit]]
        _ <- ref.set(42)
        value <- ref.get
      } yield assert(value)(equalTo(42))
    },

    testM("6. Modify a Ref to adjust mapped ref") {
      for {
        ref <- Ref.make(0)
        dependentRef = ref.map(_ * 4)
//        _ <-  ___[ZIO[Any, Nothing, Unit]] // try calling set only on ref
        _ <- ref.set(16)
        value <- dependentRef.get
      } yield assert(value)(equalTo(64))
    },

    testM("7. Modify a dependent ref to modify origin ref") {
      for {
        ref <- Ref.make(0)
        dependentRef = ref.contramap((s: String) => s.toInt)
        //        _ <-  ___[ZIO[Any, Nothing, Unit]] // this time try calling set on dependentRef
        _ <- dependentRef.set("16")
        value <- dependentRef.get
      } yield assert(value)(equalTo(16))
    },

    testM("9. Use FiberRef to add values from multiple Fibers") {
      for {
//        fiberRef <- FiberRef.make[Int](0, join = ___)
        fiberRef <- FiberRef.make[Int](0, join = _ + _)
        fiberA <- fiberRef.set(4).fork
        fiberB <- fiberRef.set(6).fork
        _ <- Fiber.joinAll(List(fiberA, fiberB))
        value <- fiberRef.get
      } yield assert(value)(equalTo(10))
    },

    testM("10. Use FiberRef to implement shopping basket stored per session (fiber)") {
      case class BasketService(basket: FiberRef[List[String]]) {
        def addItem(item: String): ZIO[Any, Nothing, Unit] =
          basket.update(item :: _)

        def removeItem(item: String): ZIO[Any, Nothing, Unit] =
          basket.update(_.filter(_ != item))

        def getItems: ZIO[Any, Nothing, List[String]] =
          basket.get
      }

      val fiber = FiberRef.make[List[String]](List.empty, join = (a, _) => a)

      val app: ZLayer[Any, Nothing, Has[BasketService]] = fiber.toLayer >>> ZLayer.fromService(BasketService)

      (for {
        basketService <- ZIO.service[BasketService]
        sessionA <- (for {
          _ <- basketService.addItem("banana")
          _ <- basketService.addItem("apple")
          _ <- basketService.removeItem("banana")
          _ <- basketService.addItem("snickers")
          items <- basketService.getItems
        } yield items).fork
        sessionB <- (for {
          _ <- basketService.addItem("milk")
          _ <- basketService.addItem("yoghurt")
          _ <- basketService.addItem("bread")
          items <- basketService.getItems
        } yield items).fork

        itemsA <- sessionA.join
        itemsB <- sessionB.join
      } yield assert(itemsA)(hasSameElements(List("apple", "snickers"))) && assert(itemsB)(hasSameElements(List("milk", "yoghurt", "bread")))
      ).provideLayer(app)
    },

    testM("11. Change value locally; already working :)") {
      for {
        fiberRef <- FiberRef.make(10)
        value <- fiberRef.locally(20)(fiberRef.get)
      } yield assert(value)(equalTo(20))
    },
  )
}
