package cloud.speelplein.data

import ChildAttendancesService.AttendancesOnDay
import cloud.speelplein.models._
import com.ibm.couchdb.Res

import scala.concurrent.Future

object ChildAttendancesService {
  case class ShiftWithAttendances(shiftId: Shift.Id, numAttendances: Int)

  case class AttendancesOnDay(uniqueChildren: Int,
                              shiftsWithAttendances: Seq[ShiftWithAttendances])
}

trait ChildAttendancesService {
  def findAttendancesForChild(childId: Child.Id)(
      implicit tenant: Tenant): Future[Seq[DayAttendance]]

  def findNumberAttendancesForChild(childId: Child.Id)(
      implicit tenant: Tenant): Future[Option[Int]]

  def findNumberOfChildAttendances(
      implicit tenant: Tenant): Future[Map[Day.Id, Map[Shift.Id, Int]]]

  def findNumberOfChildAttendances(day: DayDate, shiftId: Shift.Id)(
      implicit tenant: Tenant): Future[Int]

  def addAttendancesForChild(
      childId: Child.Id,
      day: DayDate,
      shifts: Seq[Shift.Id],
      ageGroupDate: Option[AgeGroupData] = None
  )(implicit tenant: Tenant): Future[Seq[Res.DocOk]]

  def addAttendanceForChild(childId: Child.Id,
                            day: DayDate,
                            shift: Shift.Id,
                            ageGroupData: Option[AgeGroupData] = None)(
      implicit tenant: Tenant): Future[Res.DocOk]

  def removeAttendancesForChild(
      childId: Child.Id,
      day: DayDate,
      shifts: Seq[Shift.Id])(implicit tenant: Tenant): Future[Unit]

  def removeAttendanceForChild(
      childId: Child.Id,
      day: DayDate,
      shift: Shift.Id)(implicit tenant: Tenant): Future[Unit] = {
    removeAttendancesForChild(childId, day, Seq(shift))
  }

  def findAll(
      implicit tenant: Tenant): Future[Map[Child.Id, Seq[DayAttendance]]]

  def findAllPerDay(
      implicit tenant: Tenant): Future[Map[Day.Id, AttendancesOnDay]]

  def findAllOnDay(dayId: Day.Id)(
      implicit tenant: Tenant): Future[Seq[(Child.Id, Seq[SingleAttendance])]]

  def findAllRaw(
      implicit tenant: Tenant): Future[Seq[(Day.Id, Shift.Id, Child.Id)]]

  def count(implicit tenant: Tenant): Future[Int]

}
