package cloud.speelplein.data.util

import scala.concurrent.{ Future, Promise }
import scalaz.{ -\/, \/- }
import scalaz.concurrent.Task

// TODO replace by delorean https://github.com/Verizon/delorean

object ScalazExtensions {
  implicit class PimpedScalazTask[T](task: Task[T]) {
    def toFuture: Future[T] = {
      val p: Promise[T] = Promise()
      task.unsafePerformAsync {
        case \/-(res) => p.success(res)
        case -\/(e) => p.failure(e)
      }
      p.future
    }
  }
}