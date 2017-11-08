import cloud.speelplein.dashboard.controllers.api.ApiController

import scala.concurrent.Future
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class ApiControllerSpec extends PlaySpec with Results {
  "Generic API controller" should {
    "return created" in {
      val controller = new ApiController()
      controller.created("aoeu-snth") mustBe Created(
        Json.obj("status" -> "created", "id" -> "aoeu-snth"))
    }
  }
}
