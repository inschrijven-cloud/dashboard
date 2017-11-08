package cloud.speelplein.data

import javax.inject.Inject

import cloud.speelplein.EntityWithId
import cloud.speelplein.models._
import com.norbitltd.spoiwo.model._
import com.norbitltd.spoiwo.model.enums.CellFill

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ReportService {
  def getChildrenPerDay(year: Int)(implicit tenant: Tenant): Future[Sheet]

}

class ReportServiceImpl @Inject()(
    childAttendancesService: ChildAttendancesService,
    dayService: DayService
) extends ReportService {
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
        .filter(x => DayDate.createFromDayId(x._1).get.year == year)
        .sortBy(x => DayDate.createFromDayId(x._1).get)
        .flatMap {
          case (dayId, attendances) =>
            val shiftRows = attendances.shiftsWithAttendances.map { s =>
              val shift = getShift(allDays, dayId, s.shiftId).get

              (s, shift)
            } sortBy (_._2) map {
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
