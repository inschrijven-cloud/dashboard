import cloud.speelplein.dashboard.controllers.actions.JwtAuthorizationBuilder
import cloud.speelplein.data.UserService
import cloud.speelplein.dashboard.controllers.api.TenantsController
import cloud.speelplein.data.TenantsService
import cloud.speelplein.data.couchdb.CouchDatabase
import cloud.speelplein.models.Tenant
import com.ibm.couchdb.Res
import helpers.{StubCouchDatabase, StubJwtAuthorizationBuilder}
import org.scalamock.scalatest.MockFactory
import org.scalatest.OneInstancePerTest

import scala.concurrent.Future
import org.scalatestplus.play._
import play.api.mvc._
import play.api.test._
import play.api.inject.bind
import org.scalatest.concurrent.ScalaFutures
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.json.Json

class TenantsControllerSpec
    extends PlaySpec
    with GuiceOneServerPerSuite
    with Results
    with MockFactory
    with ScalaFutures
    with OneInstancePerTest {
  val conf = Seq(
    "couchdb.host" -> "localhost",
    "couchdb.port" -> 80,
    "couchdb.https" -> false,
    "couchdb.remote.user" -> "name",
    "couchdb.remote.host" -> "remote",
    "couchdb.remote.port" -> 2222,
    "couchdb.remote.pass" -> "secret"
  )

  val tenantsService: TenantsService = mock[TenantsService]

  (tenantsService.all _)
    .expects()
    .returning(
      Future.successful(
        Seq(Tenant("some-tenant"), Tenant("another-tenant"))
      ))
    .anyNumberOfTimes()

  (tenantsService.create _)
    .expects(*)
    .returning(Future.successful(new Res.Ok))
    .anyNumberOfTimes()

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[TenantsService].toInstance(tenantsService))
    .overrides(bind[JwtAuthorizationBuilder].to[StubJwtAuthorizationBuilder])
    .overrides(bind[CouchDatabase].to[StubCouchDatabase])
    .overrides(bind(classOf[UserService]).to(mock[UserService]))
    .configure(conf: _*)
    .build()

  "The tenants controller" should {
    "display all databases on the listing page" in {
      val controller = app.injector.instanceOf[TenantsController]
      val result: Future[Result] = controller
        .list()
        .apply(FakeRequest("GET", "/blah?domain=global.speelplein.cloud"))
      contentAsJson(result) must be(
        Json.arr(Json.obj("name" -> "some-tenant"),
                 Json.obj("name" -> "another-tenant")))
    }

    "create a tenant when given a valid normalized tenant name" in {
      val controller = app.injector.instanceOf[TenantsController]
      implicit val materializer =
        app.injector.instanceOf[akka.stream.Materializer]

      val resultFut: Future[Result] = controller
        .create("test-tenant-name")
        .apply(FakeRequest("POST", "/blah?domain=global.speelplein.cloud"))

      whenReady(resultFut) { res =>
        res.header.status mustBe 201
      }

    }

    "fail to create a tenant when given a valid normalized tenant name" in {
      val controller = app.injector.instanceOf[TenantsController]
      implicit val materializer =
        app.injector.instanceOf[akka.stream.Materializer]

      val resultFut = controller
        .create("some-tenant-}{)(*&^%$#@!")
        .apply(FakeRequest("POST", "/blah?domain=global.speelplein.cloud"))

      whenReady(resultFut) { res =>
        res.header.status mustBe 400
      }
    }
  }
}
