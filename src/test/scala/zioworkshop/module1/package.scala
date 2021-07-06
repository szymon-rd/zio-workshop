package zioworkshop

import java.io.IOException
import java.net.SocketException

import zio.ZIO
import zio.clock.Clock

package object module1 {
  case class HttpClient() {
    private var n = 0 // do not use vars with ZIO :)

    def fetch(website: String): ZIO[Clock, Throwable, String] = ZIO.effect {
      if(n < 3) {
        n = n + 1
        throw new SocketException(website)
      } else s"Fetched $website!"
    }
  }
}
