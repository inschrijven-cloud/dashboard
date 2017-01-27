package be.thomastoye.speelsysteem.data

import be.thomastoye.speelsysteem.models._
import com.ibm.couchdb.Res

import scala.concurrent.Future

trait ChildAttendancesService {

  def findAttendancesForChild(childId: Child.Id): Future[Seq[DayAttendance]]

  def findNumberAttendancesForChild(childId: Child.Id): Future[Option[Int]]

  def findNumberOfChildAttendances: Future[Map[Day.Id, Map[Shift.Id, Int]]]

  def findNumberOfChildAttendances(day: DayDate, shiftId: Shift.Id): Future[Int]

  def addAttendancesForChild(childId: Child.Id, day: DayDate, shifts: Seq[Shift.Id]): Future[Seq[Res.DocOk]]

  def addAttendanceForChild(childId: Child.Id, day: DayDate, shift: Shift.Id): Future[Res.DocOk]

  def removeAttendancesForChild(childId: Child.Id, day: DayDate, shifts: Seq[Shift.Id]): Future[Unit]

  def removeAttendanceForChild(childId: Child.Id, day: DayDate, shift: Shift.Id): Future[Unit] = {
    removeAttendancesForChild(childId, day, Seq(shift))
  }

  def findAll: Future[Map[Child.Id, Seq[DayAttendance]]]

}
