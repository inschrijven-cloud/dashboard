import be.thomastoye.speelsysteem.dashboard.controllers.api.ChildApiController
import be.thomastoye.speelsysteem.data.ChildRepository
import be.thomastoye.speelsysteem.data.util.UuidService
import be.thomastoye.speelsysteem.models.Child.Id
import be.thomastoye.speelsysteem.models.Day.Id
import be.thomastoye.speelsysteem.models.{Address, Child, ContactInfo}
import be.thomastoye.speelsysteem.models.JsonFormats._
import be.thomastoye.speelsysteem.models.Shift.Id
import org.scalatestplus.play.PlaySpec
import org.scalamock.scalatest.MockFactory
import play.api.mvc.{AnyContentAsJson, Request, Results}
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Future

class ChildApiControllerSpec extends PlaySpec with Results with MockFactory {
  "ChildApiController#getById" should {
    val child = Child("first", "last", Address.empty, ContactInfo.empty, None, Seq.empty)

    val childRepo = new ChildRepository {
      override def addAttendancesForChild(id: Id, dayId: Id, shifts: Seq[Id]): Future[Option[Unit]] = ???
      override def count: Future[Int] = ???
      override def update(id: Id, child: Child): Future[Unit] = ???
      override def insert(id: Id, child: Child): Future[Id] = ???
      override def findById(id: Id): Future[Option[(Id, Child)]] = {
        id match {
          case "existing-id" => Future.successful(Some((id, child)))
          case _             => Future.successful(None)
        }
      }

      override def findAll: Future[Seq[(Id, Child)]] = ???
      override def delete(id: Id): Future[Unit] = ???
    }

    val uuidService = new UuidService {
      override def random: String = ???
    }


    "return NotFound if the child is not in the database" in {
      val controller = new ChildApiController(childRepo, uuidService)
      status(controller.getById("non-existant-id").apply(FakeRequest())) mustBe NOT_FOUND
    }

    "return child as JSON if the child is in the database" in {
      val controller = new ChildApiController(childRepo, uuidService)
      status(controller.getById("existing-id").apply(FakeRequest())) mustBe OK
      contentAsJson(controller.getById("existing-id").apply(FakeRequest())) mustBe Json.toJson(child)
    }
  }

  "ChildApiController#all" should {
    val child1 = Child("first", "last", Address.empty, ContactInfo.empty, None, Seq.empty)
    val child2 = Child("first2", "last2", Address.empty, ContactInfo.empty, None, Seq.empty)

    val uuidService = new UuidService {
      override def random: String = ???
    }


    "return list of children in the database" in {
      val childRepo = new ChildRepository {
        override def addAttendancesForChild(id: Id, dayId: Id, shifts: Seq[Id]): Future[Option[Unit]] = ???
        override def count: Future[Int] = ???
        override def update(id: Id, child: Child): Future[Unit] = ???
        override def insert(id: Id, child: Child): Future[Id] = ???
        override def findById(id: Id): Future[Option[(Id, Child)]] = ???
        override def findAll: Future[Seq[(Id, Child)]] = Future.successful(Seq(("first-id", child1), ("second-id", child2)))
        override def delete(id: Id): Future[Unit] = ???
      }

      val controller = new ChildApiController(childRepo, uuidService)
      contentAsJson(controller.all.apply(FakeRequest())) mustBe Json.arr(Json.toJson(("first-id", child1)), Json.toJson(("second-id", child2)))
    }

    "return empty JSON list if there are no children in the database" in {
      val childRepo = new ChildRepository {
        override def addAttendancesForChild(id: Id, dayId: Id, shifts: Seq[Id]): Future[Option[Unit]] = ???
        override def count: Future[Int] = ???
        override def update(id: Id, child: Child): Future[Unit] = ???
        override def insert(id: Id, child: Child): Future[Id] = ???
        override def findById(id: Id): Future[Option[(Id, Child)]] = ???
        override def findAll: Future[Seq[(Id, Child)]] = Future.successful(Seq.empty)
        override def delete(id: Id): Future[Unit] = ???
      }

      val controller = new ChildApiController(childRepo, uuidService)
      contentAsJson(controller.all.apply(FakeRequest())) mustBe Json.arr()
    }
  }

  "ChildApiController#delete" should {
    "delete a child by id" in {
      val childRepo = mock[ChildRepository]
      (childRepo.delete _).expects("the-id-to-delete").returning(Future.successful(())).once()

      val uuidService = new UuidService {
        override def random: String = ???
      }

      val controller = new ChildApiController(childRepo, uuidService)

      status(controller.delete("the-id-to-delete").apply(FakeRequest())) mustBe OK
    }
  }

  "ChildApiController#update" should {
    "update a child" in {
      val childRepo = mock[ChildRepository]
      val child = Child("first", "last", Address.empty, ContactInfo.empty, None, Seq.empty)

      (childRepo.update _).expects("the-id-to-update", child).returning(Future.successful(())).once()
      val controller = new ChildApiController(childRepo, mock[UuidService])

      val body: FakeRequest[Child] = FakeRequest().withBody[Child](child)
      status(controller.update("the-id-to-update").apply(body)) mustBe OK
    }
  }
}
