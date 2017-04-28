import helpers.StubCouchDatabaseModule
import org.scalatestplus.play._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class FullApplicationSpec extends PlaySpec with OneServerPerSuite with Results {
  implicit override lazy val app = GuiceApplicationBuilder(overrides = Seq(new StubCouchDatabaseModule())).build()

  "Starting the app" must {
    "work and be able to get heartbeat" in {
      val wsClient = app.injector.instanceOf[WSClient]
      val publicAddress = s"localhost:$port"
      val url = s"http://$publicAddress/heartbeat"
      // await is from play.api.test.FutureAwaits
      val response = await(wsClient.url(url).get())

      response.json mustBe Json.obj("status" -> "ok", "statusCode" -> 200, "message" -> "online")
    }
  }
}
