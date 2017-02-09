package be.thomastoye.speelsysteem.data

import java.time.format.DateTimeFormatter
import javax.inject.Inject

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.models._
import com.norbitltd.spoiwo.model._
import com.norbitltd.spoiwo.model.enums.CellFill
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ ExecutionContext, Future }

class FiscalCertificateService @Inject() (
    childRepository: ChildRepository,
    dayService: DayService,
    childAttendancesService: ChildAttendancesService,
    implicit val ec: ExecutionContext
) {

  case class CertificateRow(
    lastName: String,
    firstName: String,
    street: String, // street with number
    city: String, // city with zipcode
    birthDate: String,
    period: String, // e.g. 1/07/2015 tot 28/08/2015 or 1/07/2015 tot 21/08/2015 or 6/07/2015 tot 28/08/2015
    numDays: Int,
    dayPrice: String,
    totalReceivedAmount: String
  )

  def getFiscalCertificateSheet(year: Int): Future[Sheet] = for {
    allDays <- dayService.findAll
    allChildren <- childRepository.findAll
    allAttendances <- childAttendancesService.findAll
  } yield FiscalCertificateCalculator(year, allDays, allChildren, allAttendances).calculateAll

  private case class FiscalCertificateCalculator(
      year: Int,
      allDays: Seq[EntityWithId[Day.Id, Day]],
      allChildren: Seq[EntityWithId[Child.Id, Child]],
      allAttendances: Map[Child.Id, Seq[DayAttendance]]
  ) {
    def calculateAll: Sheet = {
      val certRows = allChildren.map(x => child2certificateRow(x.entity, x.id))

      val sheetDataRows = certRows.map(row =>
        Row().withCellValues(
          row.lastName,
          row.firstName,
          row.street,
          row.city,
          row.birthDate,
          row.period,
          row.numDays,
          row.dayPrice,
          row.totalReceivedAmount
        ))

      val headerStyle = CellStyle(fillPattern = CellFill.Solid, fillForegroundColor = Color.AquaMarine, font = Font(bold = true))
      val headers = Seq("Achternaam", "Voornaam", "Straat", "Adres", "Geboortedatum", "Periode", "Aantal dagen", "Prijs", "Totaal betaald")

      val certSheet = Sheet(name = "Fiscale attesten")
        .withRows(Seq(Row(style = headerStyle).withCellValues(headers: _*)) ++ sheetDataRows)
        .withColumns((0 to headers.length).map(idx => Column(index = idx, autoSized = true)): _*)

      certSheet
    }

    private def shiftIdToAmountPaidOnDay(dayId: Day.Id, shiftsAttended: Seq[Shift.Id]): Price = {
      val shifts = allDays.find(_.id == dayId).get.entity.shifts.filter(x => shiftsAttended contains x.id)
      shifts.map(_.price).fold(Price(0, 0))(_ + _)
    }

    private def getTotalPricePaidForAttendances(attendances: Seq[DayAttendance]): Price = {
      val listOfPrices: Seq[Price] = attendances.map(x => shiftIdToAmountPaidOnDay(x.day, x.shifts.map(_.shiftId)))
      val totalPrice = listOfPrices.fold(Price(0, 0))(_ + _)
      totalPrice
    }

    private def findAttendancesForChild(id: Child.Id): Seq[DayAttendance] = allAttendances.getOrElse(id, Seq.empty)

    private def child2certificateRow(child: Child, id: Child.Id): CertificateRow = {
      def dayId2Day(id: Day.Id): Day = allDays.filter(_.id == id).head.entity

      val attendances = findAttendancesForChild(id).filter(att => getDayDate(att.day).year == year)

      CertificateRow(
        child.lastName,
        child.firstName,
        child.address.street.map(_ + " ").getOrElse("") + child.address.number.getOrElse(""),
        child.address.zipCode.map(_ + " ").getOrElse("") + child.address.city.getOrElse(""),
        child.birthDate.map(_.toLocalDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).getOrElse(""),
        "Paas- en grote vakantie " + year,
        attendances.length,
        "Voormiddag €1, middag €1, namiddag €2", // TODO refactor this?
        getTotalPricePaidForAttendances(attendances).toString
      )
    }

    private def getDayDate(dayId: Day.Id): DayDate = DayDate.createFromDayId(dayId).get
  }
}
