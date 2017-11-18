import cloud.speelplein.EntityWithId
import cloud.speelplein.dashboard.controllers.ApplicationController
import cloud.speelplein.dashboard.controllers.api.DayApiController
import cloud.speelplein.data.ChildRepository
import cloud.speelplein.models.Shift.Id
import cloud.speelplein.models.Tenant
import cloud.speelplein.dashboard.controllers.actions.DomainAction
import cloud.speelplein.dashboard.controllers.api.DayApiController
import cloud.speelplein.data.{ChildRepository, DayService}
import cloud.speelplein.models.{Day, Tenant}
import helpers.StubJwtAuthorizationBuilder
import org.scalamock.scalatest.MockFactory
import org.scalatest.{AsyncFlatSpec, EitherValues}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class DomainActionSpec
    extends PlaySpec
    with Results
    with MockFactory
    with EitherValues {
  val domainAction = new DomainAction(
    new BodyParsers.Default(stubControllerComponents().parsers))

  "DomainAction" should {
    "fail when no domain is provided" in {
      val dayService = mock[DayService]

      (dayService
        .findAll(_: Tenant))
        .expects(*)
        .returning(Future.successful(Seq.empty[EntityWithId[Id, Day]]))
        .never()

      val childRepo = mock[ChildRepository]

      val controller = new DayApiController(dayService,
                                            childRepo,
                                            domainAction,
                                            new StubJwtAuthorizationBuilder())
      controller.setControllerComponents(stubControllerComponents())

      val eventualResult = controller.all.apply(FakeRequest())
      val body = contentAsJson(eventualResult)
      body mustBe Json.obj(
        "status" -> "error",
        "reason" -> "Missing or invalid 'domain' URL parameter")
      status(eventualResult) mustBe 400
    }

    "correctly refine example.speelplein.cloud" in {
      val res = Await.result(
        domainAction.refine(
          FakeRequest("GET", "/blah?domain=example.speelplein.cloud")),
        2.seconds)
      res.isRight mustBe true
      res.right.value.userDomain mustBe "example.speelplein.cloud"
      res.right.value.tenant mustBe Tenant("example")
      res.right.value.tenant.databaseName.value mustBe "ic-example"
    }
  }
}
