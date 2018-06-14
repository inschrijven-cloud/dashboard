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
  def attendancesOnDayWorkbook(dayId: Day.Id)(
      implicit tenant: Tenant): Future[Workbook]
}

class ReportServiceImpl @Inject()(
    childAttendancesService: ChildAttendancesService,
    crewAttendancesService: CrewAttendancesService,
    crewRepository: CrewRepository,
    childRepository: ChildRepository,
    dayService: DayService
) extends ReportService {

  def attendancesOnDayWorkbook(dayId: Day.Id)(
      implicit tenant: Tenant): Future[Workbook] =
    for {
      childAttendances <- childAttendancesService.findAllOnDay(dayId)
      crewAttendances <- crewAttendancesService.findAllOnDay(dayId)
      allChildren <- childRepository.findAll
      allCrew <- crewRepository.findAll
      shifts <- dayService
        .findById(dayId)
        .map(_.map(_.entity.shifts).getOrElse(Seq.empty))
    } yield {
      // Sheet 1  : All children
      // Sheet 2  : All crew
      // Sheet 3  : Day and details (price, time...)
      // Sheet 4-n: Different age groups, if existing

      // -- Sheet 1: All children on day

      val childrenWithAttendances = childAttendances
        .map {
          case (id, seq) => (allChildren.find(_.id == id).map(_.entity), seq)
        }
        .filter(_._1.isDefined)
        .map { case (child, seq) => (child.get, seq) }
        .sortBy(_._1.lastName)

      val allChildRows = childrenWithAttendances flatMap {
        case (child, attendances) =>
          attendances map { attendance =>
            val shiftDescription = shifts
              .find(_.id == attendance.shiftId)
              .map(
                shift =>
                  shift.kind.description + shift.desciption
                    .map(desc => if (desc == "") "" else " (" + desc + ")")
                    .getOrElse(""))
              .getOrElse("")
            Row().withCellValues(child.firstName,
                                 child.lastName,
                                 shiftDescription,
                                 attendance.ageGroupName.getOrElse(""))
          }
      }

      val allChildSheet = Sheet(
        name = "Alle kinderen",
        rows = {
          val headerRow =
            Row().withCellValues("Voornaam ",
                                 "Familienaam",
                                 "Dagdeel/Activiteit",
                                 "Leeftijdsgroep")

          val rows = allChildRows
          headerRow :: rows.toList
        },
        tables = List(
          Table(
            id = 1,
            cellRange = CellRange(0 -> allChildRows.length, 0 -> 3),
            style = TableStyle(TableStyleName.TableStyleMedium2,
                               showRowStripes = true),
            enableAutoFilter = true
          )
        )
      ).withColumns(
        (0 to 5).map(idx => Column(index = idx, autoSized = true)): _*)

      // -- Sheet 2: All crew on day

      val crewWithAttendances = crewAttendances
        .map {
          case (id, seq) => (allCrew.find(_.id == id).map(_.entity), seq)
        }
        .filter(_._1.isDefined)
        .map { case (child, seq) => (child.get, seq) }
        .sortBy(_._1.lastName)

      val allCrewRows = crewWithAttendances flatMap {
        case (crew, attendances) =>
          attendances map { attendance =>
            val shiftDescription = shifts
              .find(_.id == attendance.shiftId)
              .map(_.kind.description)
              .getOrElse("")
            Row().withCellValues(
              crew.firstName,
              crew.lastName,
              shiftDescription,
              crew.contact.phone.map(_.phoneNumber).headOption.getOrElse(""))
          }
      }

      val allCrewSheet = Sheet(
        name = "Alle animatoren",
        rows = {
          val headerRow =
            Row().withCellValues("Voornaam ",
                                 "Familienaam",
                                 "Dagdeel/Activiteit",
                                 "Telefoonnummer")

          val rows = allCrewRows
          headerRow :: rows.toList
        },
        tables = List(
          Table(
            id = 2,
            cellRange = CellRange(0 -> allCrewRows.length, 0 -> 3),
            style = TableStyle(TableStyleName.TableStyleMedium2,
                               showRowStripes = true),
            enableAutoFilter = true
          )
        )
      ).withColumns(
        (0 to 5).map(idx => Column(index = idx, autoSized = true)): _*)

      // -- Sheet 3: Details for shifts

      val shiftsHeader = Row().withCellValues(
        "Type",
        "Omschrijving",
        "Locatie",
        "Prijs",
        "Start",
        "Einde",
        "Kinderen kunnen aanwezig zijn",
        "Animatoren kunnan aanwezig zijn"
      )

      val shiftRows = shifts.sorted.map { shift =>
        Row().withCellValues(
          shift.kind.description,
          shift.desciption.getOrElse(""),
          shift.location.getOrElse(""),
          shift.price.toString,
          shift.startAndEnd.map(_.start.toString).getOrElse(""),
          shift.startAndEnd.map(_.end.toString).getOrElse(""),
          shift.childrenCanBePresent,
          shift.crewCanBePresent
        )
      }

      val shiftDetailsSheet = Sheet(
        name = "Dagdelen en activiteiten",
        rows = shiftsHeader :: shiftRows.toList,
        tables = List(
          Table(
            id = 3,
            cellRange = CellRange(0 -> shiftRows.length,
                                  0 -> (shiftsHeader.cells.toList.length - 1)),
            style = TableStyle(TableStyleName.TableStyleMedium2,
                               showRowStripes = true),
            enableAutoFilter = true
          ))
      ).withColumns(shiftsHeader.cells.toList.indices.map(idx =>
        Column(index = idx, autoSized = true)): _*)

      // Sheet 4-n: Age groups

      val ageGroups =
        ("" :: childAttendances
          .flatMap(_._2.map(_.ageGroupName))
          .toList
          .flatten).distinct

      val ageGroupSheets = ageGroups.zipWithIndex map {
        case (ageGroup, index) =>
          val ageGroupChildWithAttendance = childAttendances
            .map {
              case (child, seq) =>
                (child,
                 seq.filter(el =>
                   el.ageGroupName
                     .contains(ageGroup) || (el.ageGroupName.isEmpty && ageGroup.isEmpty)))
            }
            .filter(_._2.nonEmpty)
            .map {
              case (id, seq) =>
                (allChildren.find(_.id == id).map(_.entity), seq)
            }
            .filter(_._1.isDefined)
            .map { case (child, seq) => (child.get, seq) }
            .flatMap { case (child, seq) => seq.map(att => (child, att)) }
            .sortBy(tuple => (tuple._2.shiftId, tuple._1.lastName))

          val ageGroupRows = ageGroupChildWithAttendance map {
            case (child, attendance) => {
              val shiftDescription = shifts
                .find(_.id == attendance.shiftId)
                .map(
                  shift =>
                    shift.kind.description + shift.desciption
                      .map(desc => if (desc == "") "" else " (" + desc + ")")
                      .getOrElse(""))
                .getOrElse("")
              Row().withCellValues(child.firstName,
                                   child.lastName,
                                   shiftDescription)
            }
          }

          Sheet(
            name = if (ageGroup.isEmpty) "Geen leeftijdsgroep" else ageGroup,
            rows = {
              val headerRow =
                Row().withCellValues("Voornaam ",
                                     "Familienaam",
                                     "Dagdeel/Activiteit")

              headerRow :: ageGroupRows.toList
            },
            tables = List(
              Table(
                id = index + 4,
                cellRange = CellRange(0 -> ageGroupRows.length, 0 -> 2),
                style = TableStyle(TableStyleName.TableStyleMedium2,
                                   showRowStripes = true),
                enableAutoFilter = true
              )
            )
          ).withColumns((0 to 5).map(idx =>
            Column(index = idx, autoSized = true)): _*)
      }

      // When there's only one element in ageGroups => no ageGroups (except "No age groups" group) => don't show the age group sheet
      val allSheets = allChildSheet :: allCrewSheet :: shiftDetailsSheet :: (if (ageGroups.length != 1)
                                                                               ageGroupSheets.toList
                                                                             else
                                                                               List.empty)

      Workbook(allSheets: _*)
    }

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
