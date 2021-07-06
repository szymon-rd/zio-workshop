package zioworkshop

import zio.ZIO

package object module2 {
  case class HttpClient() {

    def fetch(website: String): ZIO[Any, Throwable, String] =
      ZIO.succeed(s"Fetched $website!")
  }
}
