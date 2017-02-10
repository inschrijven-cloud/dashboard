import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.dashboard.controllers.api.ChildAttendanceApiController
import be.thomastoye.speelsysteem.data.{ ChildAttendancesService, ChildRepository, DayService }
import be.thomastoye.speelsysteem.models.Shift.ShiftKind
import be.thomastoye.speelsysteem.models.{ Shift, _ }
import helpers.UnimplementedDayService
import org.scalamock.scalatest.MockFactory

import scala.concurrent.Future
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class ChildAttendanceApiControllerSpec extends PlaySpec with Results with MockFactory {
  "ChildAttendanceApiController#numberOfChildAttendances" should {
    "return a list with days and the number of children attending each shift" in {

      // Currently can't be mocked, relies on functionality in the trait
      val dayServiceStub = new UnimplementedDayService {
        def shift: (String => Shift) = Shift(_, Price(2, 0), true, true, ShiftKind.Afternoon, None, None, None)

        override def findAll: Future[Seq[EntityWithId[Shift.Id, Day]]] = Future.successful(
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
        )
      }

      val childRepo = mock[ChildRepository]

      val childAttendanceService = mock[ChildAttendancesService]

      (childAttendanceService.findNumberOfChildAttendances _).expects().returning(Future.successful(
        Map(
          "2016-11-25" -> Map("shift1" -> 2, "shift2" -> 1),
          "2016-02-01" -> Map("shift3" -> 1)
        )
      )).once()

      val controller = new ChildAttendanceApiController(childRepo, dayServiceStub, childAttendanceService)

      val body = contentAsJson(controller.numberOfChildAttendances.apply(FakeRequest()))

      body mustBe Json.obj(
        "2016-11-25" -> Json.obj("shift1" -> 2, "shift2" -> 1),
        "2016-02-01" -> Json.obj("shift3" -> 1)
      )
    }
  }
}
