import be.thomastoye.speelsysteem.dashboard.controllers.api.ChildApiController
import be.thomastoye.speelsysteem.data.ChildRepository
import be.thomastoye.speelsysteem.data.util.UuidService
import be.thomastoye.speelsysteem.models.{Address, Child, ContactInfo}
import be.thomastoye.speelsysteem.models.JsonFormats.childFormat
import be.thomastoye.speelsysteem.models.Shift.Id
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Results
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Future

class ChildApiControllerSpec extends PlaySpec with Results {
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
}
