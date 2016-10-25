import be.thomastoye.speelsysteem.data.{ChildRepository, DayService, FiscalCertificateService}
import be.thomastoye.speelsysteem.models.Child.Id
import be.thomastoye.speelsysteem.models.Shift.ShiftKind
import be.thomastoye.speelsysteem.models._
import com.norbitltd.spoiwo.model._
import com.norbitltd.spoiwo.model.enums.CellFill
import helpers.UnimplementedDayService
import org.scalatest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FiscalCertificateServiceSpec extends AsyncWordSpec with Matchers{
  "Fiscal certificate service" should {
    "return a sheet with no date rows when there are no children" in {
      val childRepo = new ChildRepository {
        override def addAttendancesForChild(id: Child.Id, dayId: Day.Id, shifts: Seq[Shift.Id]): Future[Option[Unit]] = ???
        override def count: Future[Int] = ???
        override def update(id: Child.Id, child: Child): Future[Unit] = ???
        override def insert(id: Child.Id, child: Child): Future[Child.Id] = ???
        override def findById(id: Child.Id): Future[Option[(Child.Id, Child)]] = ???
        override def findAll: Future[Seq[(Child.Id, Child)]] = Future.successful(Seq.empty)
        override def delete(id: Id): Future[Unit] = ???
      }

      val dayService = new UnimplementedDayService {
        override def findAll: Future[Seq[(Day.Id, Day)]] = Future.successful(Seq.empty)
      }

      val service = new FiscalCertificateService(childRepo, dayService, global)

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
      val children: Seq[(Child.Id, Child)] = Seq(
        ("child1", Child(
            "voornaam",
            "achternaam",
            Address(Some("straatlaan"), Some("55"), Some(9999), Some("stad")),
            ContactInfo(Seq.empty, Seq.empty),
            Some(DayDate(20, 1, 2000)),
            Seq(Attendance("day1", Seq("shift1")))
          )
        )
      )

      val days: Seq[(Day.Id, Day)] = Seq(
        ("day1", Day(DayDate(17, 5, 2016), Seq(
          Shift("shift1", Price(2, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Afternoon, None, None, None))
        ))
      )

      val childRepo = new ChildRepository {
        override def addAttendancesForChild(id: Child.Id, dayId: Day.Id, shifts: Seq[Shift.Id]): Future[Option[Unit]] = ???
        override def count: Future[Int] = ???
        override def update(id: Child.Id, child: Child): Future[Unit] = ???
        override def insert(id: Child.Id, child: Child): Future[Child.Id] = ???
        override def findById(id: Child.Id): Future[Option[(Child.Id, Child)]] = ???
        override def findAll: Future[Seq[(Child.Id, Child)]] = Future.successful(children)
        override def delete(id: Id): Future[Unit] = ???
      }

      val dayService = new UnimplementedDayService {
        override def findAll: Future[Seq[(Day.Id, Day)]] = Future.successful(days)
      }

      val service = new FiscalCertificateService(childRepo, dayService, global)

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
      val children: Seq[(Child.Id, Child)] = Seq(
        ("child1", Child(
            "voornaam1",
            "achternaam",
            Address(Some("straatlaan"), Some("55"), Some(9999), Some("stad")),
            ContactInfo(Seq.empty, Seq.empty),
            Some(DayDate(20, 1, 2000)),
            Seq(
              Attendance("day1", Seq("shift1")),
              Attendance("day2", Seq("shift5"))
            )
          )
        ),
        ("child2", Child(
            "voornaam2",
            "achternaam",
            Address(Some("straatlaan"), Some("55"), Some(9999), Some("stad")),
            ContactInfo(Seq.empty, Seq.empty),
            Some(DayDate(20, 1, 2000)),
            Seq(
              Attendance("day1", Seq("shift3")),
              Attendance("day2", Seq("shift4")),
              Attendance("day3", Seq("shift10"))
            )
          )
        ),
        ("child3", Child(
            "voornaam3",
            "achternaam",
            Address(Some("straatlaan"), Some("55"), Some(9999), Some("stad")),
            ContactInfo(Seq.empty, Seq.empty),
            Some(DayDate(20, 1, 2000)),
            Seq(
              Attendance("day1", Seq("shift1", "shift2", "shift3")),
              Attendance("day3", Seq("shift7", "shift8", "shift9"))
            )
          )
        )
      )

      val days: Seq[(Day.Id, Day)] = Seq(
        ("day1", Day(DayDate(17, 5, 2015), Seq(
          Shift("shift1", Price(1, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Morning, None, None, None),
          Shift("shift2", Price(1, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Noon, None, None, None),
          Shift("shift3", Price(2, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Afternoon, None, None, None))
        )),
        ("day2", Day(DayDate(18, 5, 2016), Seq(
          Shift("shift4", Price(1, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Morning, None, None, None),
          Shift("shift5", Price(1, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Noon, None, None, None),
          Shift("shift6", Price(2, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Afternoon, None, None, None))
        )),
        ("day3", Day(DayDate(19, 5, 2015), Seq(
          Shift("shift7", Price(1, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Morning, None, None, None),
          Shift("shift8", Price(1, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Noon, None, None, None),
          Shift("shift9", Price(2, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.Afternoon, None, None, None),
          Shift("shift10", Price(20, 0), childrenCanBePresent = true, crewCanBePresent = true, ShiftKind.External, None, None, None))
        ))
      )

      val childRepo = new ChildRepository {
        override def addAttendancesForChild(id: Child.Id, dayId: Day.Id, shifts: Seq[Shift.Id]): Future[Option[Unit]] = ???
        override def count: Future[Int] = ???
        override def update(id: Child.Id, child: Child): Future[Unit] = ???
        override def insert(id: Child.Id, child: Child): Future[Child.Id] = ???
        override def findById(id: Child.Id): Future[Option[(Child.Id, Child)]] = ???
        override def findAll: Future[Seq[(Child.Id, Child)]] = Future.successful(children)
        override def delete(id: Id): Future[Unit] = ???
      }

      val dayService = new UnimplementedDayService {
        override def findAll: Future[Seq[(Day.Id, Day)]] = Future.successful(days)
      }

      val service = new FiscalCertificateService(childRepo, dayService, global)

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
