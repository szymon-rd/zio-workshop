package zioworkshop

import zio.test.{Spec, TestFailure, TestSuccess, ZTestEnv}

object Common {
  type OurSpec = Spec[ZTestEnv, TestFailure[Any], TestSuccess]
  def ___[T]: T = ???.asInstanceOf[T]
}
