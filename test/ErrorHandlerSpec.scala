import scala.concurrent.Future
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class ErrorHandlerSpec extends PlaySpec with Results {
  "Error handler" should {
    "return JSON on server error" in {
      val handler = new ErrorHandler()
      val header = FakeRequest()
      await(handler.onServerError(header, new Exception())).body.contentType must be(Some("application/json"))
    }

    "return JSON on client error" in {
      val handler = new ErrorHandler()
      val header = FakeRequest()
      await(handler.onClientError(header, 403)).body.contentType must be(Some("application/json"))
    }

    "return status code on client error" in {
      val handler = new ErrorHandler()
      val header = FakeRequest()
      (contentAsJson(handler.onClientError(header, 403)) \ "statusCode").as[Int] must be(403)
    }
  }

}
