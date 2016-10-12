import be.thomastoye.speelsysteem.data.couchdb.{CouchDayService, DayService}
import be.thomastoye.speelsysteem.models.Child.Id
import be.thomastoye.speelsysteem.models._

import scala.concurrent.Future
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class DayServiceSpec extends PlaySpec with FutureAwaits with DefaultAwaitTimeout {
  "DayService#findNumberOfChildAttendances" should {
    "show number of children that were present per day" in {
      val children = Seq(
        Child("", "", Address(), ContactInfo(Seq(), Seq()), None, Seq(Attendance("2016-05-03", Seq("shift1", "shift3")), Attendance("2016-05-25", Seq("shiftB")))),
        Child("", "", Address(), ContactInfo(Seq(), Seq()), None, Seq(Attendance("2016-05-03", Seq("shift1")), Attendance("2016-05-25", Seq("shiftC"))))
      )

      def shiftCreator(id: String): Shift = Shift(id, Price(1, 0), true, true, Shift.ShiftKind.Afternoon, None, None, None)

      val days = Seq(
        Day(DayDate(3, 5, 2016), Seq(shiftCreator("shift1"), shiftCreator("shift2"), shiftCreator("shift3"))),
        Day(DayDate(25, 5, 2016), Seq(shiftCreator("shiftA"), shiftCreator("shiftB"), shiftCreator("shiftC")))
      )

      val dayService = new DayService() {
        override def findAll: Future[Seq[(Id, Day)]] = Future.successful(Seq(
          ("2016-05-03", days.head), ("2016-05-25", days.last)
        ))

        override def findAttendancesForChild(id: Id): Future[Seq[Day]] = ???
      }

      await(dayService.findNumberOfChildAttendances(children)) must be(Seq(
        ("2016-05-03", Seq( ("shift1", 2), ("shift2", 0), ("shift3", 1) )),
        ("2016-05-25", Seq( ("shiftA", 0), ("shiftB", 1), ("shiftC", 1) ))
      ))
    }
  }

}
