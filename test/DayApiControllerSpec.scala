import be.thomastoye.speelsysteem.dashboard.controllers.api.DayApiController
import be.thomastoye.speelsysteem.data.{ChildRepository, DayService}
import be.thomastoye.speelsysteem.models.Shift.{Id, ShiftKind}
import be.thomastoye.speelsysteem.models.{Shift, _}

import scala.concurrent.Future
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class DayApiControllerSpec extends PlaySpec with Results {
  "DayApiController#all" should {
    "return an empty JSON array if there are no days" in {
      val dayServiceStub = new DayService {
        override def findAll: Future[Seq[(Id, Day)]] = Future.successful(Seq.empty[(Id, Day)])

        override def findAttendancesForChild(id: Id): Future[Seq[Day]] = ???
      }

      val childRepoStub = new ChildRepository {
        override def addAttendancesForChild(id: Id, dayId: Id, shifts: Seq[Id]): Future[Option[Unit]] = ???
        override def count: Future[Port] = ???
        override def update(id: Id, child: Child): Future[Unit] = ???
        override def insert(id: Id, child: Child): Future[Id] = ???
        override def findById(id: Id): Future[Option[(Id, Child)]] = ???
        override def findAll: Future[Seq[(Id, Child)]] = ???
      }

      val controller = new DayApiController(dayServiceStub, childRepoStub)

      val body = contentAsJson(controller.all.apply(FakeRequest()))
      body mustBe Json.arr()
    }

    "return a JSON array with all days" in {
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
        override def findAll: Future[Seq[(Id, Child)]] = ???
      }

      val controller = new DayApiController(dayServiceStub, childRepoStub)

      val body = contentAsJson(controller.all.apply(FakeRequest()))
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

  "DayApiController#numberOfChildAttendances" should {
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

      val controller = new DayApiController(dayServiceStub, childRepoStub)

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
