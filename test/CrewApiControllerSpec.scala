import cloud.speelplein.EntityWithId
import cloud.speelplein.dashboard.controllers.api.CrewApiController
import cloud.speelplein.models._
import cloud.speelplein.models.JsonFormats._
import cloud.speelplein.dashboard.controllers.actions.TenantAction
import cloud.speelplein.dashboard.controllers.api.CrewApiController
import cloud.speelplein.data.CrewRepository
import cloud.speelplein.models.{Crew, Tenant}
import helpers.StubJwtAuthorizationBuilder
import org.scalatestplus.play.PlaySpec
import org.scalamock.scalatest.MockFactory
import play.api.mvc.{BodyParsers, Results}
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CrewApiControllerSpec extends PlaySpec with Results with MockFactory {
  val tenantAction = new TenantAction(
    new BodyParsers.Default(stubControllerComponents().parsers))
  val fakeReq = FakeRequest("GET", "/blah?tenant=blah")
  val authBuilder = new StubJwtAuthorizationBuilder()

  "CrewApiController#getById" should {
    val crew = Crew("first",
                    "last",
                    Address.empty,
                    true,
                    None,
                    ContactInfo.empty,
                    None,
                    None)

    "return NotFound if the crew member is not in the database" in {
      val crewRepo = mock[CrewRepository]
      (crewRepo
        .findById(_: String)(_: Tenant))
        .expects("existing-id", *)
        .returning(Future.successful(Some(EntityWithId("existing-id", crew))))
        .never()
      (crewRepo
        .findById(_: String)(_: Tenant))
        .expects(*, *)
        .returning(Future.successful(None))
        .once()

      val controller =
        new CrewApiController(crewRepo, tenantAction, authBuilder)
      controller.setControllerComponents(stubControllerComponents())

      status(controller.getById("non-existant-id").apply(fakeReq)) mustBe NOT_FOUND
    }

    "return crew member as JSON if the crew member is in the database" in {
      val crewRepo = mock[CrewRepository]
      (crewRepo
        .findById(_: String)(_: Tenant))
        .expects("existing-id", *)
        .returning(Future.successful(Some(EntityWithId("existing-id", crew))))
        .once()
      (crewRepo
        .findById(_: String)(_: Tenant))
        .expects(*, *)
        .returning(Future.successful(None))
        .never()

      val controller =
        new CrewApiController(crewRepo, tenantAction, authBuilder)
      controller.setControllerComponents(stubControllerComponents())

      val res = controller.getById("existing-id").apply(fakeReq)
      status(res) mustBe OK
      contentAsJson(res) mustBe Json.toJson(crew)
    }
  }

  "CrewApiController#all" should {
    val crew1 = Crew("first",
                     "last",
                     Address.empty,
                     true,
                     None,
                     ContactInfo.empty,
                     None,
                     None)
    val crew2 = Crew("first2",
                     "last2",
                     Address.empty,
                     true,
                     None,
                     ContactInfo.empty,
                     None,
                     None)

    "return list of crew members in the database" in {
      val crewRepo = mock[CrewRepository]
      (crewRepo
        .findAll(_: Tenant))
        .expects(*)
        .returning(Future.successful(Seq(EntityWithId("first-id", crew1),
                                         EntityWithId("second-id", crew2))))
        .once()

      val controller =
        new CrewApiController(crewRepo, tenantAction, authBuilder)
      controller.setControllerComponents(stubControllerComponents())

      contentAsJson(controller.all.apply(fakeReq)) mustBe Json.arr(
        Json.toJson(EntityWithId("first-id", crew1)),
        Json.toJson(EntityWithId("second-id", crew2)))
    }

    "return empty JSON list if there are no crew members in the database" in {
      val crewRepo = mock[CrewRepository]

      (crewRepo
        .findAll(_: Tenant))
        .expects(*)
        .returning(Future.successful(Seq.empty))

      val controller =
        new CrewApiController(crewRepo, tenantAction, authBuilder)
      controller.setControllerComponents(stubControllerComponents())

      contentAsJson(controller.all.apply(fakeReq)) mustBe Json.arr()
    }
  }

  "CrewApiController#delete" should {
    "delete a crew member by id" in {
      val crewRepo = mock[CrewRepository]
      (crewRepo
        .delete(_: String)(_: Tenant))
        .expects("the-id-to-delete", *)
        .returning(Future.successful(()))
        .once()

      val controller =
        new CrewApiController(crewRepo, tenantAction, authBuilder)
      controller.setControllerComponents(stubControllerComponents())

      status(controller.delete("the-id-to-delete").apply(fakeReq)) mustBe OK
    }
  }

  "CrewApiController#update" should {
    "update a crew member" in {
      val crewRepo = mock[CrewRepository]
      val crew = Crew("first",
                      "last",
                      Address.empty,
                      true,
                      None,
                      ContactInfo.empty,
                      None,
                      None)

      (crewRepo
        .update(_: String, _: Crew)(_: Tenant))
        .expects("the-id-to-update", crew, *)
        .returning(Future.successful(()))
        .once()
      val controller =
        new CrewApiController(crewRepo, tenantAction, authBuilder)
      controller.setControllerComponents(stubControllerComponents())

      val body: FakeRequest[Crew] =
        fakeReq.withMethod("POST").withBody[Crew](crew)
      status(controller.update("the-id-to-update").apply(body)) mustBe OK
    }
  }
}
