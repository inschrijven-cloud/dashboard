package be.thomastoye.speelsysteem.data

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.models.Child.Id
import be.thomastoye.speelsysteem.models.{Child, Day, Shift}
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

trait DayService {

  def findAll: Future[Seq[EntityWithId[Id, Day]]]

  def findAttendancesForChild(id: Id): Future[Seq[Day]]

  def findNumberOfChildAttendances(allChildren: Seq[Child]): Future[Seq[(Day.Id, Seq[(Shift.Id, Int)])]] = {
    def numberOfChildrenAttending(day: Day.Id, shift: Shift.Id): Int = {
      allChildren
        .map(child => child.attendances.filter(att => att.day == day).map(att => att.shifts.count(_ == shift)).sum)
        .sum
    }

    findAll.map { all =>
      all.map { entityWithId =>
        val childrenAttendingPerShiftOnThisDay = entityWithId.entity.shifts.map { shift =>
          (shift.id, numberOfChildrenAttending(entityWithId.id, shift.id))
        }

        (entityWithId.id, childrenAttendingPerShiftOnThisDay)
      }
    }
  }

  def insert(day: Day): Future[Unit]

  def findById(id: Day.Id): Future[Option[Day]]

  def update(id: Day.Id, day: Day): Future[Unit]
}
