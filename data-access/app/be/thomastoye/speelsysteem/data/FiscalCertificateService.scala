package be.thomastoye.speelsysteem.data

import java.time.format.DateTimeFormatter
import javax.inject.Inject

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.models._
import com.norbitltd.spoiwo.model._
import com.norbitltd.spoiwo.model.enums.CellFill

import scala.concurrent.{ExecutionContext, Future}


class FiscalCertificateService @Inject()(childRepository: ChildRepository, dayService: DayService, implicit val ec: ExecutionContext) {

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

  private def shiftIdToAmountPaidOnDay(allDays: Seq[EntityWithId[Day.Id, Day]], dayId: Day.Id, shiftsAttended: Seq[Shift.Id]): Price = {
    val shifts = allDays.find(_.id == dayId).get.entity.shifts.filter(x => shiftsAttended contains x.id)
    shifts.map(_.price).fold(Price(0, 0))(_ + _)
  }

  private def getTotalPricePaidForAttendances(attendances: Seq[Attendance], allDays: Seq[EntityWithId[Day.Id, Day]]): Price = {
    val listOfPrices: Seq[Price] = attendances.map(x => shiftIdToAmountPaidOnDay(allDays, x.day, x.shifts))
    val totalPrice = listOfPrices.fold(Price(0, 0))(_ + _)
    totalPrice
  }

  private def child2certificateRow(child: Child, year: Int, allDays: Seq[EntityWithId[Day.Id, Day]]): CertificateRow = {
    def dayId2Day(id: Day.Id): Day = allDays.filter(_.id == id).head.entity

    val attendances = child.attendances.filter(att => dayId2Day(att.day).date.year == year)

    CertificateRow(
      child.lastName,
      child.firstName,
      child.address.street.map(_ + " ").getOrElse("") + child.address.number.getOrElse(""),
      child.address.zipCode.map(_ + " ").getOrElse("") + child.address.city.getOrElse(""),
      child.birthDate.map(_.toLocalDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).getOrElse(""),
      "Paas- en grote vakantie " + year,
      attendances.length,
      "Voormiddag €1, middag €1, namiddag €2", // TODO refactor this?
      getTotalPricePaidForAttendances(attendances, allDays).toString
    )
  }

  def getFiscalCertificateSheet(year: Int): Future[Sheet] = {
    val certRows = for {
      allDays <- dayService.findAll
      children <- childRepository.findAll
    } yield {
      children.map(_.entity).map(child2certificateRow(_, year, allDays))
    }

    val sheetDataRowsFut = certRows.map(_.map { row =>
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
      )
    })

    sheetDataRowsFut map { sheetDataRows =>
      val headerStyle = CellStyle(fillPattern = CellFill.Solid, fillForegroundColor = Color.AquaMarine, font = Font(bold = true))

      val certSheet = Sheet(name = "Fiscale attesten")
        .withRows(
          Seq(
            Row(style = headerStyle).withCellValues(
              "Achternaam",
              "Voornaam",
              "Straat",
              "Adres",
              "Geboortedatum",
              "Periode",
              "Aantal dagen",
              "Prijs",
              "Totaal betaald"
            )
          ) ++ sheetDataRows
        )
        .withColumns(
          (0 to 8).map(idx => Column(index = idx, autoSized = true)):_*
        )

      certSheet
    }
  }
}
