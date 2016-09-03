import be.thomastoye.speelsysteem.dashboard.controllers.ApplicationController

import scala.concurrent.Future
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class FullApplicationSpec extends PlaySpec with Results {
  "/heartbeat endpoint" should {
    "return ok" in {
      val controller = new ApplicationController()
      val result: Future[Result] = controller.heartbeat().apply(FakeRequest())
      val body = contentAsJson(result)
      body mustBe Json.obj("status" -> "ok", "statusCode" -> 200, "message" -> "online")
    }
  }
}
