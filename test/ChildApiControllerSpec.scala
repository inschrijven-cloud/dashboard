import cloud.speelplein.EntityWithId
import cloud.speelplein.dashboard.controllers.api.ChildApiController
import cloud.speelplein.models._
import cloud.speelplein.models.JsonFormats._
import cloud.speelplein.dashboard.controllers.actions.TenantAction
import cloud.speelplein.dashboard.controllers.api.ChildApiController
import cloud.speelplein.data.ChildRepository
import cloud.speelplein.models.Tenant
import helpers.StubJwtAuthorizationBuilder
import org.scalatestplus.play.PlaySpec
import org.scalamock.scalatest.MockFactory
import play.api.mvc.{AnyContentAsJson, BodyParsers, Request, Results}
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ChildApiControllerSpec extends PlaySpec with Results with MockFactory {
  val tenantAction = new TenantAction(
    new BodyParsers.Default(stubControllerComponents().parsers))
  val fakeReq = FakeRequest("GET", "/blah?tenant=blah")
  val authBuilder = new StubJwtAuthorizationBuilder()

  "ChildApiController#getById" should {
    val child = Child("first",
                      "last",
                      Address.empty,
                      ContactInfo.empty,
                      None,
                      Seq.empty,
                      None,
                      MedicalInformation.empty,
                      None)

    "return NotFound if the child is not in the database" in {
      val childRepo = mock[ChildRepository]
      (childRepo
        .findById(_: String)(_: Tenant))
        .expects("existing-id", *)
        .returning(Future.successful(Some(EntityWithId("existing-id", child))))
        .never()
      (childRepo
        .findById(_: String)(_: Tenant))
        .expects(*, *)
        .returning(Future.successful(None))
        .once()

      val controller =
        new ChildApiController(childRepo, tenantAction, authBuilder)
      controller.setControllerComponents(stubControllerComponents())

      status(controller.getById("non-existant-id").apply(fakeReq)) mustBe NOT_FOUND
    }

    "return child as JSON if the child is in the database" in {
      val childRepo = mock[ChildRepository]
      (childRepo
        .findById(_: String)(_: Tenant))
        .expects("existing-id", *)
        .returning(Future.successful(Some(EntityWithId("existing-id", child))))
        .once()
      (childRepo
        .findById(_: String)(_: Tenant))
        .expects(*, *)
        .returning(Future.successful(None))
        .never()

      val controller =
        new ChildApiController(childRepo, tenantAction, authBuilder)
      controller.setControllerComponents(stubControllerComponents())

      val res = controller.getById("existing-id").apply(fakeReq)
      status(res) mustBe OK
      contentAsJson(res) mustBe Json.toJson(child)
    }
  }

  "ChildApiController#all" should {
    val child1 = Child("first",
                       "last",
                       Address.empty,
                       ContactInfo.empty,
                       None,
                       Seq.empty,
                       None,
                       MedicalInformation.empty,
                       None)
    val child2 = Child("first2",
                       "last2",
                       Address.empty,
                       ContactInfo.empty,
                       None,
                       Seq.empty,
                       None,
                       MedicalInformation.empty,
                       None)

    "return list of children in the database" in {
      val childRepo = mock[ChildRepository]
      (childRepo
        .findAll(_: Tenant))
        .expects(*)
        .returning(Future.successful(Seq(EntityWithId("first-id", child1),
                                         EntityWithId("second-id", child2))))
        .once()

      val controller =
        new ChildApiController(childRepo, tenantAction, authBuilder)
      controller.setControllerComponents(stubControllerComponents())

      contentAsJson(controller.all.apply(fakeReq)) mustBe Json.arr(
        Json.toJson(EntityWithId("first-id", child1)),
        Json.toJson(EntityWithId("second-id", child2)))
    }

    "return empty JSON list if there are no children in the database" in {
      val childRepo = mock[ChildRepository]

      (childRepo
        .findAll(_: Tenant))
        .expects(*)
        .returning(Future.successful(Seq.empty))

      val controller =
        new ChildApiController(childRepo, tenantAction, authBuilder)
      controller.setControllerComponents(stubControllerComponents())

      contentAsJson(controller.all.apply(fakeReq)) mustBe Json.arr()
    }
  }

  "ChildApiController#delete" should {
    "delete a child by id" in {
      val childRepo = mock[ChildRepository]
      (childRepo
        .delete(_: String)(_: Tenant))
        .expects("the-id-to-delete", *)
        .returning(Future.successful(()))
        .once()

      val controller =
        new ChildApiController(childRepo, tenantAction, authBuilder)
      controller.setControllerComponents(stubControllerComponents())

      status(controller.delete("the-id-to-delete").apply(fakeReq)) mustBe OK
    }
  }

  "ChildApiController#update" should {
    "update a child" in {
      val childRepo = mock[ChildRepository]
      val child = Child("first",
                        "last",
                        Address.empty,
                        ContactInfo.empty,
                        None,
                        Seq.empty,
                        None,
                        MedicalInformation.empty,
                        None)

      (childRepo
        .update(_: String, _: Child)(_: Tenant))
        .expects("the-id-to-update", child, *)
        .returning(Future.successful(()))
        .once()
      val controller =
        new ChildApiController(childRepo, tenantAction, authBuilder)
      controller.setControllerComponents(stubControllerComponents())

      val body: FakeRequest[Child] =
        fakeReq.withMethod("POST").withBody[Child](child)
      status(controller.update("the-id-to-update").apply(body)) mustBe OK
    }
  }
}
