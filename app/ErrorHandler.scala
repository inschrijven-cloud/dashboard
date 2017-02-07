import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent._
import javax.inject.Singleton

import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json.Json

@Singleton
class ErrorHandler extends HttpErrorHandler with StrictLogging {

  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Future.successful(
      Status(statusCode)(Json.obj(
        "status" -> "error",
        "statusCode" -> statusCode,
        "message" -> message
      ))
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    logger.error(exception.toString, exception)

    Future.successful(
      InternalServerError(Json.obj(
        "status" -> "error",
        "message" -> exception.getMessage,
        "stacktrace" -> exception.getStackTrace.mkString("\n")
      ))
    )
  }
}
