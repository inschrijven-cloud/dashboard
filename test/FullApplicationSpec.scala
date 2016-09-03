import be.thomastoye.speelsysteem.data.couchdb.CouchDatabase
import com.google.inject.AbstractModule
import com.ibm.couchdb.CouchDbApi

import org.scalatestplus.play._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class StubCouchDatabase extends CouchDatabase {
  override val db: CouchDbApi = null
}

class StubCouchDatabaseModule extends AbstractModule {
  override def configure() = {
    bind(classOf[CouchDatabase]).to(classOf[StubCouchDatabase])
  }
}

class FullApplicationSpec extends PlaySpec with OneServerPerSuite with Results {

  // Override app if you need a Application with other than default parameters.
  implicit override lazy val app = GuiceApplicationBuilder(overrides = Seq(new StubCouchDatabaseModule())).build()

  "Starting the app" must {
    "work and be able to get heartbeat" in {
      val wsClient = app.injector.instanceOf[WSClient]
      val myPublicAddress =  s"localhost:$port"
      val testPaymentGatewayURL = s"http://$myPublicAddress/heartbeat"
      // await is from play.api.test.FutureAwaits
      val response = await(wsClient.url(testPaymentGatewayURL).get())

      response.json mustBe Json.obj("status" -> "ok", "statusCode" -> 200, "message" -> "online")
    }
  }
}
