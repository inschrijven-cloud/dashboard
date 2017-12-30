package cloud.speelplein.data

import cloud.speelplein.data.CrewAttendancesService.AttendancesOnDay
import cloud.speelplein.models._
import com.ibm.couchdb.Res

import scala.concurrent.Future

object CrewAttendancesService {
  case class ShiftWithAttendances(shiftId: Shift.Id, numAttendances: Int)
  case class AttendancesOnDay(uniqueCrew: Int,
                              shiftsWithAttendances: Seq[ShiftWithAttendances])
}

trait CrewAttendancesService {
  def findAttendancesForCrew(crewId: Crew.Id)(
      implicit tenant: Tenant): Future[Seq[DayAttendance]]

  def findNumberAttendancesForCrew(crewId: Crew.Id)(
      implicit tenant: Tenant): Future[Option[Int]]

  def findNumberOfCrewAttendances(
      implicit tenant: Tenant): Future[Map[Day.Id, Map[Shift.Id, Int]]]

  def findNumberOfCrewAttendances(day: DayDate, shiftId: Shift.Id)(
      implicit tenant: Tenant): Future[Int]

  def addAttendancesForCrew(
      crewId: Crew.Id,
      day: DayDate,
      shifts: Seq[Shift.Id])(implicit tenant: Tenant): Future[Seq[Res.DocOk]]

  def addAttendanceForCrew(crewId: Crew.Id, day: DayDate, shift: Shift.Id)(
      implicit tenant: Tenant): Future[Res.DocOk]

  def removeAttendancesForCrew(
      crewId: Crew.Id,
      day: DayDate,
      shifts: Seq[Shift.Id])(implicit tenant: Tenant): Future[Unit]

  def removeAttendanceForCrew(crewId: Crew.Id, day: DayDate, shift: Shift.Id)(
      implicit tenant: Tenant): Future[Unit] = {
    removeAttendancesForCrew(crewId, day, Seq(shift))
  }

  def findAll(implicit tenant: Tenant): Future[Map[Crew.Id, Seq[DayAttendance]]]

  def findAllPerDay(
      implicit tenant: Tenant): Future[Map[Day.Id, AttendancesOnDay]]

  def findAllRaw(
      implicit tenant: Tenant): Future[Seq[(Day.Id, Shift.Id, Crew.Id)]]

}
