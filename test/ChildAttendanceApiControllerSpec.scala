import be.thomastoye.speelsysteem.dashboard.controllers.api.ChildAttendanceApiController
import be.thomastoye.speelsysteem.data.{ChildRepository, DayService}
import be.thomastoye.speelsysteem.models.Shift.{Id, ShiftKind}
import be.thomastoye.speelsysteem.models.{Shift, _}

import scala.concurrent.Future
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class ChildAttendanceApiControllerSpec extends PlaySpec with Results {
  "ChildAttendanceApiController#numberOfChildAttendances" should {
    "return a list with days and the number of children attending each shift" in {
      val dayServiceStub = new DayService {
        def shift: (String => Shift) = Shift(_, Price(2,0), true, true, ShiftKind.Afternoon, None, None, None)

        override def findAll: Future[Seq[(Shift.Id, Day)]] = Future.successful(
          Seq(
            ("2016-11-25", Day(DayDate(25, 11, 2016), Seq(
              Shift("shift1", Price(1,0), true, true, ShiftKind.Morning, None, None, None),
              Shift("shift2", Price(2,0), true, true, ShiftKind.Afternoon, None, None, None)
            ))),
            ("2016-02-01", Day(DayDate(1, 2, 2016), Seq(
              Shift("shift3", Price(2,0), false, false, ShiftKind.Afternoon, Some("location"), Some("description"),
                Some(StartAndEndTime(RelativeTime(13, 0), RelativeTime(17, 30))))
            )))
          )
        )

        override def findAttendancesForChild(id: Id): Future[Seq[Day]] = ???
      }

      val childRepoStub = new ChildRepository {
        override def addAttendancesForChild(id: Id, dayId: Id, shifts: Seq[Id]): Future[Option[Unit]] = ???
        override def count: Future[Port] = ???
        override def update(id: Id, child: Child): Future[Unit] = ???
        override def insert(id: Id, child: Child): Future[Id] = ???
        override def findById(id: Id): Future[Option[(Id, Child)]] = ???
        override def findAll: Future[Seq[(Id, Child)]] = Future.successful(Seq(
          ("child1",
            Child("aoeu1", "snth", Address(), ContactInfo(Seq.empty, Seq.empty), None,
              Seq(Attendance("2016-11-25", Seq("shift1"))))
            ),
          ("child2",
            Child("aoeu2", "snth", Address(), ContactInfo(Seq.empty, Seq.empty), None,
              Seq(Attendance("2016-11-25", Seq("shift1", "shift2")), Attendance("2016-02-01", Seq("shift3"))))
            )
        ))
      }

      val controller = new ChildAttendanceApiController(childRepoStub, dayServiceStub)

      val body = contentAsJson(controller.numberOfChildAttendances.apply(FakeRequest()))

      body mustBe Json.arr(
        Json.obj(
          "dayId" -> "2016-11-25",
          "shifts" -> Json.arr(
            Json.obj("id" -> "shift1", "numChildAttendances" -> 2),
            Json.obj("id" -> "shift2", "numChildAttendances" -> 1)
          )
        ),
        Json.obj(
          "dayId" -> "2016-02-01",
          "shifts" -> Json.arr(
            Json.obj("id" -> "shift3", "numChildAttendances" -> 1)
          )
        )
      )
    }
  }
}
