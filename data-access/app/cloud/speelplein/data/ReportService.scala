package cloud.speelplein.data

import javax.inject.Inject

import cloud.speelplein.EntityWithId
import cloud.speelplein.models._
import com.norbitltd.spoiwo.model._
import com.norbitltd.spoiwo.model.enums.CellFill

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ReportService {
  def getChildrenPerDay(year: Int)(implicit tenant: Tenant): Future[Sheet]
  def getCrewCompensation(year: Int)(implicit tenant: Tenant): Future[Sheet]
}

class ReportServiceImpl @Inject()(
    childAttendancesService: ChildAttendancesService,
    crewAttendancesService: CrewAttendancesService,
    crewRepository: CrewRepository,
    dayService: DayService
) extends ReportService {

  override def getCrewCompensation(year: Int)(
      implicit tenant: Tenant): Future[Sheet] = {
    for {
      allAttendances <- crewAttendancesService.findAllRaw
      allDays <- dayService.findAll.map(_.filter(_.entity.date.year == year))
      allCrew <- crewRepository.findAll
    } yield {

      // return true if crew member did attend specified shift on day
      def crewDidAttendShift(crewId: Crew.Id,
                             dayId: Day.Id,
                             shiftId: Shift.Id): Boolean = {
        allAttendances.contains((dayId, shiftId, crewId))
      }

      val shifts = allDays
        .sortBy(_.entity.date)
        .map(day => day.entity)
        .map(day => Day(day.date, day.shifts.sorted))
        .flatMap(day => day.shifts.map(shift => (day.date, shift)))

      val crewAttendances =
        allCrew.filter(_.entity.active).sortBy(_.entity.lastName).map {
          crewWithId =>
            // Convert crew to a list of 1's and 0's (did or did not attend shift)
            val attendances = shifts.map(
              shiftWithDate =>
                crewDidAttendShift(crewWithId.id,
                                   shiftWithDate._1.getDayId,
                                   shiftWithDate._2.id))

            (crewWithId.id, crewWithId.entity.fullName, attendances)
        }

      val headerStyle = CellStyle(fillPattern = CellFill.Solid,
                                  fillForegroundColor = Color.AquaMarine,
                                  font = Font(bold = true))

      val header = Seq(
        Row()
          .withStyle(headerStyle)
          .withCellValues("" +: shifts.map(_._1.toString): _*),
        Row()
          .withStyle(headerStyle)
          .withCellValues("" +: shifts.map(_._2.kind.description): _*)
      )

      val body = crewAttendances.map {
        case (crewId, fullName, attendances) =>
          val cells: Seq[Cell] = Cell(fullName) +: attendances.map(bool =>
            if (bool) Cell(1) else Cell(0))

          Row().withCells(cells)
      }

      Sheet(name = s"Aanwezigheden animatoren ($year)")
        .withRows(header ++ body)
        .withColumns((0 to shifts.length + 1).map(idx =>
          Column(index = idx, autoSized = true)): _*)

    }
  }

  override def getChildrenPerDay(year: Int)(
      implicit tenant: Tenant): Future[Sheet] = {
    for {
      allAttendances <- childAttendancesService.findAllPerDay
      allDays <- dayService.findAll
    } yield {
      val headerStyle = CellStyle(fillPattern = CellFill.Solid,
                                  fillForegroundColor = Color.AquaMarine,
                                  font = Font(bold = true))
      val headers =
        Seq("Dag", "Dagdeel", "Aantal kinderen", "Unieke kinderen op dag")

      val sheetDataRows: Iterable[Row] = allAttendances.toSeq
        .filter {
          case (dayId, attOnDay) =>
            DayDate.createFromDayId(dayId).get.year == year
        }
        .sortBy { case (dayId, attOnDay) => DayDate.createFromDayId(dayId).get }
        .flatMap {
          case (dayId, attendances) =>
            val shiftRows = attendances.shiftsWithAttendances.map { s =>
              val shift = getShift(allDays, dayId, s.shiftId).get

              (s, shift)
            } sortBy { case (shiftWithAttendances, shift) => shift } map {
              case (shiftWithAttendances, shift) =>
                Row().withCellValues("",
                                     shift.kind.description,
                                     shiftWithAttendances.numAttendances)
            }

            Seq(
              Row()
                .withCellValues(
                  DayDate.createFromDayId(dayId).get.toString,
                  "",
                  "",
                  attendances.uniqueChildren
                )
                .withStyle(
                  CellStyle().copy(
                    font = Some(Font().copy(italic = Some(true))))
                )
            ) ++ shiftRows
        }

      val certSheet = Sheet(name = s"Aantal kinderen per dag ($year)")
        .withRows(
          Seq(Row(style = headerStyle).withCellValues(headers: _*)) ++ sheetDataRows)
        .withColumns((0 to headers.length).map(idx =>
          Column(index = idx, autoSized = true)): _*)

      certSheet
    }
  }

  private def getShift(allDays: Seq[EntityWithId[Day.Id, Day]],
                       dayId: Day.Id,
                       shiftId: Shift.Id): Option[Shift] = {
    allDays
      .find(_.id == dayId)
      .flatMap(_.entity.shifts.find(_.id == shiftId))
  }
}
