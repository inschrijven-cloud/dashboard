import cloud.speelplein.EntityWithId
import cloud.speelplein.data.{ ChildAttendancesService, ChildRepository, FiscalCertificateService }
import cloud.speelplein.models.Child.Id
import cloud.speelplein.models.Shift.ShiftKind
import cloud.speelplein.models._
import cloud.speelplein.data.{ ChildAttendancesService, ChildRepository, DayService, FiscalCertificateService }
import cloud.speelplein.models._
import com.norbitltd.spoiwo.model._
import com.norbitltd.spoiwo.model.enums.CellFill
import org.scalamock.scalatest.MockFactory
import org.scalatest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FiscalCertificateServiceSpec extends AsyncWordSpec with Matchers with MockFactory {
  implicit val testTenant = Tenant("test")

  "Fiscal certificate service" should {
    "return a sheet with no date rows when there are no children" in {
      val childRepo = mock[ChildRepository]
      (childRepo.findAll(_: Tenant)).expects(*).returning(Future.successful(Seq.empty)).once()

      val dayService = mock[DayService]
      (dayService.findAll(_: Tenant)).expects(*).returning(Future.successful(Seq.empty))

      val childAttendanceService = mock[ChildAttendancesService]
      (childAttendanceService.findAll(_: Tenant)).expects(*).returning(Future.successful(Map.empty)).once()

      val service = new FiscalCertificateService(childRepo, dayService, childAttendanceService, global)

      val headerStyle = CellStyle(fillPattern = CellFill.Solid, fillForegroundColor = Color.AquaMarine, font = Font(bold = true))

      val expected = Sheet(name = "Fiscale attesten")
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
          )
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

      service.getFiscalCertificateSheet(2016) map { sheet =>
        sheet should be(expected)
      }
    }

    "return a sheet with data for one child with one attendance" in {
      val children: Seq[EntityWithId[Child.Id, Child]] = Seq(
        EntityWithId("child1", Child(
          "voornaam",
          "achternaam",
          Address(Some("straatlaan"), Some("55"), Some(9999), Some("stad")),
          ContactInfo(Seq.empty, Seq.empty),
          None,
          Seq.empty,
          Some(DayDate(20, 1, 2000)),
          MedicalInformation.empty,
          None
        ))
      )

      val days: Seq[EntityWithId[Day.Id, Day]] = Seq(
        EntityWithId("2016-05-17", Day(DayDate(17, 5, 2016), Seq(
          Shift("shift1", Price(2, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Afternoon, None, None, None)
        )))
      )

      val childRepo = mock[ChildRepository]
      (childRepo.findAll(_: Tenant)).expects(*).returning(Future.successful(children))

      val dayService = mock[DayService]
      (dayService.findAll(_: Tenant)).expects(*).returning(Future.successful(days)).once()

      val childAttendanceService = mock[ChildAttendancesService]
      (childAttendanceService.findAll(_: Tenant)).expects(*).returning(Future.successful(
        Map(
          "child1" -> Seq(DayAttendance("2016-03-07", Seq(SingleAttendance("shift1", None, None))))
        )
      )).once()

      val service = new FiscalCertificateService(childRepo, dayService, childAttendanceService, global)

      val headerStyle = CellStyle(fillPattern = CellFill.Solid, fillForegroundColor = Color.AquaMarine, font = Font(bold = true))

      val expected = Sheet(name = "Fiscale attesten")
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
          ) ++ Seq(
              Row().withCellValues(
                "achternaam",
                "voornaam",
                "straatlaan 55",
                "9999 stad",
                "20/01/2000",
                "Paas- en grote vakantie 2016",
                1,
                "Voormiddag €1, middag €1, namiddag €2",
                "€2.00"
              )
            )
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

      service.getFiscalCertificateSheet(2016) map { sheet =>
        sheet should be(expected)
      }
    }

    "return a sheet with data for multiple children" in {
      val children: Seq[EntityWithId[Child.Id, Child]] = Seq(
        EntityWithId("child1", Child(
          "voornaam1",
          "achternaam",
          Address(Some("straatlaan"), Some("55"), Some(9999), Some("stad")),
          ContactInfo(Seq.empty, Seq.empty),
          None,
          Seq.empty,
          Some(DayDate(20, 1, 2000)),
          MedicalInformation.empty,
          None
        )),
        EntityWithId("child2", Child(
          "voornaam2",
          "achternaam",
          Address(Some("straatlaan"), Some("55"), Some(9999), Some("stad")),
          ContactInfo(Seq.empty, Seq.empty),
          None,
          Seq.empty,
          Some(DayDate(20, 1, 2000)),
          MedicalInformation.empty,
          None
        )),
        EntityWithId("child3", Child(
          "voornaam3",
          "achternaam",
          Address(Some("straatlaan"), Some("55"), Some(9999), Some("stad")),
          ContactInfo(Seq.empty, Seq.empty),
          None,
          Seq.empty,
          Some(DayDate(20, 1, 2000)),
          MedicalInformation.empty,
          None
        ))
      )

      val days: Seq[EntityWithId[Day.Id, Day]] = Seq(
        EntityWithId("2015-05-17", Day(DayDate(17, 5, 2015), Seq(
          Shift("shift1", Price(1, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Morning, None, None, None),
          Shift("shift2", Price(1, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Noon, None, None, None),
          Shift("shift3", Price(2, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Afternoon, None, None, None)
        ))),
        EntityWithId("2016-05-18", Day(DayDate(18, 5, 2016), Seq(
          Shift("shift4", Price(1, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Morning, None, None, None),
          Shift("shift5", Price(1, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Noon, None, None, None),
          Shift("shift6", Price(2, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Afternoon, None, None, None)
        ))),
        EntityWithId("2015-05-19", Day(DayDate(19, 5, 2015), Seq(
          Shift("shift7", Price(1, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Morning, None, None, None),
          Shift("shift8", Price(1, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Noon, None, None, None),
          Shift("shift9", Price(2, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Afternoon, None, None, None),
          Shift("shift10", Price(20, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.External, None, None, None)
        )))
      )

      val childRepo = mock[ChildRepository]
      (childRepo.findAll(_: Tenant)).expects(*).returning(Future.successful(children))

      val dayService = mock[DayService]
      (dayService.findAll(_: Tenant)).expects(*).returning(Future.successful(days)).once()

      val childAttendanceService = mock[ChildAttendancesService]
      (childAttendanceService.findAll(_: Tenant)).expects(*).returning(Future.successful(
        Map(
          "child1" -> Seq(
            DayAttendance("2016-03-05", Seq(SingleAttendance("shift1"))),
            DayAttendance("2016-03-06", Seq(SingleAttendance("shift5")))
          ),
          "child2" -> Seq(
            DayAttendance("2016-03-05", Seq(SingleAttendance("shift3"))),
            DayAttendance("2016-03-06", Seq(SingleAttendance("shift4"))),
            DayAttendance("2016-03-07", Seq(SingleAttendance("shift10")))
          ),
          "child3" -> Seq(
            DayAttendance("2016-03-05", Seq(
              SingleAttendance("shift1"),
              SingleAttendance("shift2"),
              SingleAttendance("shift3")
            )),
            DayAttendance("2016-03-07", Seq(
              SingleAttendance("shift7"),
              SingleAttendance("shift8"),
              SingleAttendance("shift9")
            ))
          )
        )
      )).once()

      val service = new FiscalCertificateService(childRepo, dayService, childAttendanceService, global)

      val headerStyle = CellStyle(fillPattern = CellFill.Solid, fillForegroundColor = Color.AquaMarine, font = Font(bold = true))

      val expected = Sheet(name = "Fiscale attesten")
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
          ) ++ Seq(
              Row().withCellValues(
                "achternaam",
                "voornaam1",
                "straatlaan 55",
                "9999 stad",
                "20/01/2000",
                "Paas- en grote vakantie 2015",
                1,
                "Voormiddag €1, middag €1, namiddag €2",
                "€1.00"
              ),
              Row().withCellValues(
                "achternaam",
                "voornaam2",
                "straatlaan 55",
                "9999 stad",
                "20/01/2000",
                "Paas- en grote vakantie 2015",
                2,
                "Voormiddag €1, middag €1, namiddag €2",
                "€22.00"
              ),
              Row().withCellValues(
                "achternaam",
                "voornaam3",
                "straatlaan 55",
                "9999 stad",
                "20/01/2000",
                "Paas- en grote vakantie 2015",
                2,
                "Voormiddag €1, middag €1, namiddag €2",
                "€8.00"
              )
            )
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

      service.getFiscalCertificateSheet(2015) map { sheet =>
        sheet should be(expected)
      }
    }
  }

}
