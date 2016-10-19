import helpers.{StubChildRepositoryModule, StubCouchDatabaseModule}

import scala.concurrent.Future
import org.scalatestplus.play._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import play.test.WithApplication


class CorsSpec extends PlaySpec with OneServerPerSuite {
  implicit override lazy val app = GuiceApplicationBuilder(overrides = Seq(new StubChildRepositoryModule(), new StubCouchDatabaseModule())).build()

  "CORS" should {

    "CORS headers should be set when requesting an API endpoint" in {

      val result = route(app, FakeRequest(GET, "/api/v1/child")).get

      status(result) must be(200)

      headers(result) must contain (
        ACCESS_CONTROL_ALLOW_ORIGIN -> "*",
        ACCESS_CONTROL_ALLOW_METHODS -> "GET, POST, DELETE, PUT",
        ACCESS_CONTROL_ALLOW_HEADERS -> "Content-Type, Authorization",
        ACCESS_CONTROL_ALLOW_CREDENTIALS -> "true"
      )
    }

    "always return ok for a CORS preflight OPTIONS request" in {

      val result = route(app, FakeRequest(OPTIONS, "/heartbeat")).get

      status(result) must be(200)
      headers(result) must contain(
        ACCESS_CONTROL_ALLOW_ORIGIN -> "*",
        ACCESS_CONTROL_ALLOW_METHODS -> "GET, POST, DELETE, PUT",
        ACCESS_CONTROL_ALLOW_HEADERS -> "Content-Type, Authorization",
        ACCESS_CONTROL_ALLOW_CREDENTIALS -> "true"
      )
    }
  }
}
