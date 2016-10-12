package be.thomastoye.speelsysteem.data.couchdb

import be.thomastoye.speelsysteem.models.Child.Id
import be.thomastoye.speelsysteem.models.Day._
import be.thomastoye.speelsysteem.models.{Child, Day, Shift}
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

trait DayService {

  def findAll: Future[Seq[(Id, Day)]]

  def findAttendancesForChild(id: Id): Future[Seq[Day]]

  def findNumberOfChildAttendances(allChildren: Seq[Child]): Future[Seq[(Day.Id, Seq[(Shift.Id, Int)])]] = {
    def numberOfChildrenAttending(day: Day.Id, shift: Shift.Id): Int = {
      allChildren
        .map(child => child.attendances.filter(att => att.day == day).map(att => att.shifts.count(_ == shift)).sum)
        .sum
    }

    findAll.map { all =>
      all.map { tuple =>
        val childrenAttendingPerShiftOnThisDay = tuple._2.shifts.map { shift =>
          (shift.id, numberOfChildrenAttending(tuple._1, shift.id))
        }

        (tuple._1, childrenAttendingPerShiftOnThisDay)
      }
    }
  }
}
