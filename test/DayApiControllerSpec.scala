import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.dashboard.controllers.actions.DomainAction
import be.thomastoye.speelsysteem.dashboard.controllers.api.DayApiController
import be.thomastoye.speelsysteem.data.{ ChildRepository, DayService }
import be.thomastoye.speelsysteem.models.Shift.{ Id, ShiftKind }
import be.thomastoye.speelsysteem.models._
import be.thomastoye.speelsysteem.models.JsonFormats._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatestplus.play._
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class DayApiControllerSpec extends PlaySpec with Results with MockFactory {
  val domainAction = new DomainAction(new BodyParsers.Default(stubControllerComponents().parsers))
  val fakeReq = FakeRequest("GET", "/blah?domain=test.speelplein.cloud")

  "DayApiController#all" should {

    "return an empty JSON array if there are no days" in {
      val dayService = mock[DayService]

      (dayService.findAll(_: Tenant)).expects(*).returning(Future.successful(Seq.empty[EntityWithId[Id, Day]])).once()

      val childRepo = mock[ChildRepository]

      val controller = new DayApiController(dayService, childRepo, domainAction)
      controller.setControllerComponents(stubControllerComponents())

      val body = contentAsJson(controller.all.apply(fakeReq))
      body mustBe Json.arr()
    }

    "return a JSON array with all days" in {
      val dayService = mock[DayService]

      (dayService.findAll(_: Tenant)).expects(*).returning(Future.successful(
        Seq(
          EntityWithId("2016-11-25", Day(DayDate(25, 11, 2016), Seq(
            Shift("shift1", Price(1, 0), true, true, ShiftKind.Morning, None, None, None),
            Shift("shift2", Price(2, 0), true, true, ShiftKind.Afternoon, None, None, None)
          ))),
          EntityWithId("2016-02-01", Day(DayDate(1, 2, 2016), Seq(
            Shift("shift3", Price(2, 0), false, false, ShiftKind.Afternoon, Some("location"), Some("description"),
              Some(StartAndEndTime(RelativeTime(13, 0), RelativeTime(17, 30))))
          )))
        )
      )).once()

      val childRepo = mock[ChildRepository]

      val controller = new DayApiController(dayService, childRepo, domainAction)
      controller.setControllerComponents(stubControllerComponents())

      val body = contentAsJson(controller.all.apply(fakeReq))
      body mustBe Json.arr(
        Json.obj(
          "id" -> "2016-11-25",
          "date" -> Json.obj("day" -> 25, "month" -> 11, "year" -> 2016),
          "shifts" -> Json.arr(
            Json.obj(
              "id" -> "shift1",
              "price" -> Json.obj("euro" -> 1, "cents" -> 0),
              "childrenCanBePresent" -> true,
              "crewCanBePresent" -> true,
              "kind" -> "VM"
            ),
            Json.obj(
              "id" -> "shift2",
              "price" -> Json.obj("euro" -> 2, "cents" -> 0),
              "childrenCanBePresent" -> true,
              "crewCanBePresent" -> true,
              "kind" -> "NM"
            )
          )
        ),
        Json.obj(
          "id" -> "2016-02-01",
          "date" -> Json.obj("day" -> 1, "month" -> 2, "year" -> 2016),
          "shifts" -> Json.arr(
            Json.obj(
              "id" -> "shift3",
              "price" -> Json.obj("euro" -> 2, "cents" -> 0),
              "childrenCanBePresent" -> false,
              "crewCanBePresent" -> false,
              "kind" -> "NM",
              "location" -> "location",
              "description" -> "description",
              "startAndEnd" -> Json.obj(
                "start" -> Json.obj("hour" -> 13, "minute" -> 0),
                "end" -> Json.obj("hour" -> 17, "minute" -> 30)
              )
            )
          )
        )
      )
    }
  }

  "DayApiController#create" should {
    "create a day" in {
      val day = Day(DayDate(22, 11, 2020), Seq(Shift("aoeu", Price(2, 5), true, true, ShiftKind.Afternoon, None, None, None)))

      val dayService = mock[DayService]

      (dayService.insert(_: Day)(_: Tenant)).expects(day, *).returning(Future.successful(())).once()

      val controller = new DayApiController(dayService, mock[ChildRepository], domainAction)
      controller.setControllerComponents(stubControllerComponents())

      status(controller.create.apply(fakeReq.withMethod("POST").withBody(day))) mustBe OK
    }
  }

  "DayApiController#update" should {
    "update a day" in {
      val day = Day(DayDate(22, 11, 2020), Seq(Shift("aoeu", Price(2, 5), true, true, ShiftKind.Afternoon, None, None, None)))

      val dayService = mock[DayService]

      (dayService.update(_: String, _: Day)(_: Tenant)).expects("day-id", day, *).returning(Future.successful(())).once()

      val controller = new DayApiController(dayService, mock[ChildRepository], domainAction)
      controller.setControllerComponents(stubControllerComponents())

      status(controller.update("day-id").apply(fakeReq.withMethod("POST").withBody(day))) mustBe OK
    }
  }

  "DayApiController#getById" should {
    "find existing day" in {
      val day = Day(DayDate(22, 11, 2020), Seq(Shift("aoeu", Price(2, 5), true, true, ShiftKind.Afternoon, None, None, None)))

      val dayService = mock[DayService]

      (dayService.findById(_: String)(_: Tenant)).expects("day-id", *).returning(Future.successful(
        Some(EntityWithId("day-id", day))
      )).once()

      val controller = new DayApiController(dayService, mock[ChildRepository], domainAction)
      controller.setControllerComponents(stubControllerComponents())

      val res: Future[Result] = controller.getById("day-id").apply(fakeReq)

      status(res) mustBe OK
      contentAsJson(res) must be(Json.toJson(EntityWithId("day-id", day)))
    }

    "return not found for non-existant day" in {
      val day = Day(DayDate(22, 11, 2020), Seq(Shift("aoeu", Price(2, 5), true, true, ShiftKind.Afternoon, None, None, None)))

      val dayService = mock[DayService]

      (dayService.findById(_: String)(_: Tenant)).expects(*, *).returning(Future.successful(None)).once()

      val controller = new DayApiController(dayService, mock[ChildRepository], domainAction)
      controller.setControllerComponents(stubControllerComponents())

      status(controller.getById("day-id").apply(fakeReq)) mustBe NOT_FOUND
    }
  }
}
