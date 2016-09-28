package be.thomastoye.speelsysteem.dashboard.controllers.api

import java.io.File
import java.time.format.DateTimeFormatter
import javax.inject.Inject

import be.thomastoye.speelsysteem.data.ChildRepository
import be.thomastoye.speelsysteem.data.couchdb.CouchDayService
import be.thomastoye.speelsysteem.models._
import com.norbitltd.spoiwo.model.enums.CellFill
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits._
import com.norbitltd.spoiwo.model._
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._
import com.typesafe.scalalogging.StrictLogging


class FileController @Inject()(childRepository: ChildRepository, couchDayService: CouchDayService) extends Controller with StrictLogging {

  case class CertificateRow(
    lastName: String,
    firstName: String,
    street: String, // street with number
    city: String, // city with zipcode
    birthDate: String,
    period: String, // e.g. 1/07/2015 tot 28/08/2015 or 1/07/2015 tot 21/08/2015 or 6/07/2015 tot 28/08/2015
    numDays: String,
    dayPrice: String,
    totalReceivedAmount: String
  )

  private def shiftIdToAmountPaidOnDay(allDays: Seq[(Day.Id, Day)], dayId: Day.Id, shiftsAttended: Seq[Shift.Id]): Price = {
    val shifts = allDays.find(_._1 == dayId).get._2.shifts.filter(x => shiftsAttended contains x.id)
    shifts.map(_.price).fold(Price(0, 0))(_ + _)
  }

  private def getTotalPricePaidForAttendances(attendances: Seq[Attendance], allDays: Seq[(Day.Id, Day)]): Price = {
    val listOfPrices: Seq[Price] = attendances.map(x => shiftIdToAmountPaidOnDay(allDays, x.day, x.shifts))
    val totalPrice = listOfPrices.fold(Price(0, 0))(_ + _)
    totalPrice
  }

  private def child2certificateRow(child: Child, year: Int, allDays: Seq[(Day.Id, Day)]): CertificateRow = {
    val attendances = child.attendances.filter(_.day.split('-').head.toInt == year)

    CertificateRow(
      child.lastName,
      child.firstName,
      child.address.street.map(_ + " ").getOrElse("") + child.address.number.getOrElse(""),
      child.address.zipCode.map(_ + " ").getOrElse("") + child.address.city.getOrElse(""),
      child.birthDate.map(_.toLocalDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).getOrElse(""),
      "Paas- en grote vakantie 2016",
      attendances.length.toString,
      "Voormiddag €1, middag €1, namiddag €2",
      getTotalPricePaidForAttendances(attendances, allDays).toString
    )
  }

  def downloadFiscalCertificates(year: Int) = Action.async {
    val certRows = for {
      allDays <- couchDayService.findAll
      children <- childRepository.findAll
    } yield {
      children.map(_._2).map(child2certificateRow(_, year, allDays))
    }

    val sheetDataRowsFut = certRows.map(_.map { row =>
      Row().withCellValues(row.lastName, row.firstName, row.street, row.city, row.birthDate, row.period, row.numDays, row.dayPrice, row.totalReceivedAmount)
    })

    sheetDataRowsFut map { sheetDataRows =>
      val headerStyle = CellStyle(fillPattern = CellFill.Solid, fillForegroundColor = Color.AquaMarine, font = Font(bold = true))

      val certSheet = Sheet(name = "Fiscale attesten")
        .withRows(
          Seq(
            Row(style = headerStyle).withCellValues("Achternaam", "Voornaam", "Straat", "Adres", "Geboortedatum", "Periode", "Aantal dagen", "Prijs", "Totaal betaald")
          ) ++ sheetDataRows
        )
        .withColumns(
          Column(index = 0, autoSized = true),
          Column(index = 1, autoSized = true),
          Column(index = 2, autoSized = true),
          Column(index = 3, autoSized = true),
          Column(index = 4, autoSized = true),
          Column(index = 5, autoSized = true),
          Column(index = 6, autoSized = true),
          Column(index = 7, autoSized = true),
          Column(index = 8, autoSized = true)
        )

      val file = File.createTempFile("fiscale-attesten.xlsx", System.nanoTime().toString)
      certSheet.saveAsXlsx(file.getAbsolutePath)

      Ok.sendFile(file, fileName = _ => "fiscale-attesten.xlsx")
    }
  }
}
