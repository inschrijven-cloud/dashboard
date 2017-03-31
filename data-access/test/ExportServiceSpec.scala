import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.data.{ ChildRepository, CrewRepository, ExportService }
import be.thomastoye.speelsysteem.models._
import com.norbitltd.spoiwo.model._
import com.norbitltd.spoiwo.model.enums.CellFill
import org.scalamock.scalatest.MockFactory
import org.scalatest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

class ExportServiceSpec extends WordSpec with Matchers with MockFactory {
  val headerStyle = CellStyle(fillPattern = CellFill.Solid, fillForegroundColor = Color.AquaMarine, font = Font(bold = true))

  "ExportService#childSheet" should {
    "export a sheet with all children" in {
      val crewRepo = mock[CrewRepository]
      val childRepo = mock[ChildRepository]

      val children = Seq(
        EntityWithId[Child.Id, Child]("child1", Child("firstName", "lastName", Address(
          Some("street"), Some("number"), Some(6666), Some("city")
        ), ContactInfo(Seq(PhoneContact("phoneNumber", Some("kind"), Some("comment"))), Seq("aoeu@example.com")), None, Seq.empty,
          Some(DayDate(22, 12, 2016)), MedicalInformation.empty, None)),
        EntityWithId[Child.Id, Child]("child2", Child("John", "Doe", Address.empty, ContactInfo(Seq(
          PhoneContact("phoneNumber", Some("kind"), Some("comment")), PhoneContact("phone2", None, None),
          PhoneContact("phone3", Some("landline"), None)
        ), Seq("aoeu@example.com", "secondary@example.com")), None, Seq.empty, None, MedicalInformation.empty, None))
      )

      (childRepo.findAll _).expects().returns(Future.successful(children))

      val service = new ExportService(childRepo, crewRepo, global)

      val expected: Sheet = Sheet(name = "Alle kinderen")
        .withRows(
          Seq(
            Row(style = headerStyle).withCellValues(
              "Id",
              "Voornaam",
              "Familienaam",
              "Geboortedatum",
              "Straat",
              "Nummer",
              "Postcode",
              "Stad",
              "Email",
              "Telefoon"
            ),
            Row().withCellValues("child1", "firstName", "lastName", "2016-12-22", "street", "number", "6666", "city",
              "aoeu@example.com", "phoneNumber (comment) (kind)"),
            Row().withCellValues("child2", "John", "Doe", "", "", "", "", "", "aoeu@example.com, secondary@example.com", "phoneNumber (comment) (kind), phone2, phone3 (landline)")
          )
        ).withColumns(
            (0 to 9).map(idx => Column(index = idx, autoSized = true)): _*
          )

      Await.result(service.childSheet, 1.second) should be(expected)

    }
  }

  "ExportService#crewSheet" should {
    "return a sheet with all crew members" in {
      val childRepo = mock[ChildRepository]
      val crewRepo = mock[CrewRepository]

      val crew = Seq(
        EntityWithId[Crew.Id, Crew](
          "crew1", Crew("first", "last", Address(Some("street"), Some("number"), Some(6666), Some("city")), Some("bank"),
            ContactInfo(Seq(PhoneContact("phoneNumber", Some("kind"), Some("comment"))), Seq("aoeu@example.com")),
            Some(2006), Some(DayDate(27, 7, 1997)))
        )
      )

      (crewRepo.findAll _).expects().returning(Future.successful(crew))

      val service = new ExportService(childRepo, crewRepo, global)

      val expected: Sheet = Sheet(name = "Alle animatoren")
        .withRows(
          Seq(
            Row(style = headerStyle).withCellValues(
              "Id",
              "Voornaam",
              "Familienaam",
              "Geboortedatum",
              "Jaar gestart",
              "Straat",
              "Nummer",
              "Postcode",
              "Stad",
              "Rekeningnummer",
              "Email",
              "Telefoon"
            ),
            Row().withCellValues("crew1", "first", "last", "1997-07-27", "2006", "street", "number", "6666", "city", "bank", "aoeu@example.com", "phoneNumber (comment) (kind)")
          )
        ).withColumns(
            (0 to 11).map(idx => Column(index = idx, autoSized = true)): _*
          )

      Await.result(service.crewSheet, 1.second) should be(expected)

    }
  }
}
