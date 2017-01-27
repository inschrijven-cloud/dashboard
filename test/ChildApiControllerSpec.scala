import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.dashboard.controllers.api.ChildApiController
import be.thomastoye.speelsysteem.data.ChildRepository
import be.thomastoye.speelsysteem.data.util.UuidService
import be.thomastoye.speelsysteem.models.{Address, Child, ContactInfo}
import be.thomastoye.speelsysteem.models.JsonFormats._
import org.scalatestplus.play.PlaySpec
import org.scalamock.scalatest.MockFactory
import play.api.mvc.{AnyContentAsJson, Request, Results}
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Future

class ChildApiControllerSpec extends PlaySpec with Results with MockFactory {
  "ChildApiController#getById" should {
    val child = Child("first", "last", Address.empty, ContactInfo.empty, None)

    val uuidService = mock[UuidService]

    "return NotFound if the child is not in the database" in {
      val childRepo = mock[ChildRepository]
      (childRepo.findById _).expects("existing-id").returning(Future.successful(Some(EntityWithId("existing-id", child)))).never()
      (childRepo.findById _).expects(*).returning(Future.successful(None)).once()

      val controller = new ChildApiController(childRepo, uuidService)
      status(controller.getById("non-existant-id").apply(FakeRequest())) mustBe NOT_FOUND
    }

    "return child as JSON if the child is in the database" in {
      val childRepo = mock[ChildRepository]
      (childRepo.findById _).expects("existing-id").returning(Future.successful(Some(EntityWithId("existing-id", child)))).once()
      (childRepo.findById _).expects(*).returning(Future.successful(None)).never()

      val controller = new ChildApiController(childRepo, uuidService)
      val res = controller.getById("existing-id").apply(FakeRequest())
      status(res) mustBe OK
      contentAsJson(res) mustBe Json.toJson(child)
    }
  }

  "ChildApiController#all" should {
    val child1 = Child("first", "last", Address.empty, ContactInfo.empty, None)
    val child2 = Child("first2", "last2", Address.empty, ContactInfo.empty, None)

    val uuidService = mock[UuidService]

    "return list of children in the database" in {
      val childRepo = mock[ChildRepository]
      (childRepo.findAll _).expects().returning(Future.successful(Seq(EntityWithId("first-id", child1), EntityWithId("second-id", child2)))).once()

      val controller = new ChildApiController(childRepo, uuidService)
      contentAsJson(controller.all.apply(FakeRequest())) mustBe Json.arr(Json.toJson(EntityWithId("first-id", child1)), Json.toJson(EntityWithId("second-id", child2)))
    }

    "return empty JSON list if there are no children in the database" in {
      val childRepo = mock[ChildRepository]

      (childRepo.findAll _).expects().returning(Future.successful(Seq.empty))

      val controller = new ChildApiController(childRepo, uuidService)
      contentAsJson(controller.all.apply(FakeRequest())) mustBe Json.arr()
    }
  }

  "ChildApiController#delete" should {
    "delete a child by id" in {
      val childRepo = mock[ChildRepository]
      (childRepo.delete _).expects("the-id-to-delete").returning(Future.successful(())).once()

      val uuidService = mock[UuidService]

      val controller = new ChildApiController(childRepo, uuidService)

      status(controller.delete("the-id-to-delete").apply(FakeRequest())) mustBe OK
    }
  }

  "ChildApiController#update" should {
    "update a child" in {
      val childRepo = mock[ChildRepository]
      val child = Child("first", "last", Address.empty, ContactInfo.empty, None)

      (childRepo.update _).expects("the-id-to-update", child).returning(Future.successful(())).once()
      val controller = new ChildApiController(childRepo, mock[UuidService])

      val body: FakeRequest[Child] = FakeRequest().withBody[Child](child)
      status(controller.update("the-id-to-update").apply(body)) mustBe OK
    }
  }
}
