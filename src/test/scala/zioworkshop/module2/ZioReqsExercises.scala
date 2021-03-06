package zioworkshop.module2
import java.time.{OffsetDateTime, ZoneOffset}

import zio.clock.Clock
import zio.test.Assertion.{equalTo, _}
import zio.test.{DefaultRunnableSpec, _}
import zio.test.environment.{TestClock, testEnvironment}
import zio.{Has, ZIO, ZLayer}
import zioworkshop.Common.{OurSpec, ___}

object ZioReqsExercises extends DefaultRunnableSpec {

  def spec: OurSpec = suite("ZioReqsExercises")(
    testM("1. Provide HttpClient") {
      val fetchGoogle = ZIO.accessM[HttpClient](_.fetch("www.google.com"))
      val client = HttpClient()

      // TODO Provide the client to fetchGoogle
      val fetchGoogleProvided = ___[ZIO[Any, Throwable, String]]

      assertM(fetchGoogleProvided)(containsString("google"))
    },

    testM("2. Access FooService") {
      case class FooService() {
        def foo = "foo"
      }
      case class BarService() {
        def bar = "bar"
      }
      val env: Has[FooService] with Has[BarService] = Has(FooService()) ++ Has(BarService())

      // TODO Extract foo from FooService in env
      val foo = ___[ZIO[Has[FooService], Nothing, String]].provide(env)

      assertM(foo)(equalTo("foo"))
    },

    testM("3. Construct simple layer") {
      case class FooService(client: HttpClient) {
        def fetchAndEnhance(): ZIO[Any, Throwable, String] =
          client.fetch("www.google.com").map(_.replace("oo", "ooo"))
      }

      // TODO Construct layer with FooService from HttpClient
      val layer: ZLayer[Any, Nothing, Has[FooService]] = ___

      (for {
        fooService <- ZIO.service[FooService]
        enhanced <- fooService.fetchAndEnhance()
      } yield assert(enhanced)(containsString("gooogle"))).provideLayer(layer)
    },

    testM("4. Construct layer with dependencies") {
      case class FooService(client: HttpClient) {
        def getFoo: ZIO[Any, Throwable, String] = client.fetch("www.foo.com")
      }

      case class BarService(client: HttpClient) {
        def getBar: ZIO[Any, Throwable, String] = client.fetch("www.bar.com")
      }

      type Services = Has[FooService] with Has[BarService]

      // TODO Construct layer with FooService and BarService that requires HttpClient
      val services = ___[ZLayer[Has[HttpClient], Nothing, Services]]

      val live = ZLayer.succeed(HttpClient()) >>> services
      (for {
        fooService <- ZIO.service[FooService]
        foo <- fooService.getFoo
        barService <- ZIO.service[BarService]
        bar <- barService.getBar
      } yield assert(foo)(containsString("foo")) && assert(bar)(containsString("bar"))).provideLayer(live)
    },

    testM("5. Construct app layers") {
      case class FooRepo() {
        def getFoo(id: Int): ZIO[Any, Nothing, String] = ZIO.succeed(s"foo$id")
      }

      case class BarRepo() {
        def getBar(id: Int): ZIO[Any, Nothing, String] = ZIO.succeed(s"bar$id")
      }

      case class FooService(fooRepo: FooRepo) {
        def getFirstFoo: ZIO[Any, Nothing, String] = fooRepo.getFoo(0)
      }

      case class BarService(barRepo: BarRepo) {
        def getFirstBar: ZIO[Any, Nothing, String] = barRepo.getBar(0)
      }

      case class AppView(fooService: FooService, barService: BarService) {
        def handleRequest(request: String) = request match {
          case "/foo" => fooService.getFirstFoo
          case "/bar" => barService.getFirstBar
        }
      }

      type Repos    = Has[FooRepo] with Has[BarRepo]
      type Services = Has[FooService] with Has[BarService]
      type View     = Has[AppView]


      // TODO Construct app layers
      val repoLayer    = ___[ZLayer[Any, Nothing, Repos]]
      val serviceLayer = ___[ZLayer[Repos, Nothing, Services]]
      val viewLayer    = ___[ZLayer[Services, Nothing, View]]


      val app = repoLayer >+> serviceLayer >+> viewLayer

      (for {
        view <- ZIO.service[AppView]
        firstFoo <- view.handleRequest("/foo")
        firstBar <- view.handleRequest("/bar")
      } yield assert(firstFoo)(equalTo("foo0")) && assert(firstBar)(equalTo("bar0"))).provideLayer(app)
    },

    testM("6. Use ZEnv to fetch current time") {
      for {
        _ <- TestClock.setDateTime(OffsetDateTime.of(2021, 7, 21, 0, 0, 0, 0, ZoneOffset.UTC))
        // TODO Access current date time from ZEnv Clock
        time <- ___[ZIO[Clock, Throwable, OffsetDateTime]]
      } yield assert(time.getDayOfMonth)(equalTo(21))
      // That's why we use ZEnv's clock, we can set current time or make time steps with intervals using `adjust`
    }.provideLayer(testEnvironment)
  )
}
