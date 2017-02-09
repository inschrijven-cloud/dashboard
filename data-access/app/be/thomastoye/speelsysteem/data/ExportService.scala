package be.thomastoye.speelsysteem.data

import javax.inject.Inject

import com.norbitltd.spoiwo.model.enums.CellFill
import com.norbitltd.spoiwo.model._

import scala.concurrent.{ ExecutionContext, Future }

class ExportService @Inject() (childRepository: ChildRepository, crewRepository: CrewRepository, implicit val ec: ExecutionContext) {
  private val headerStyle = CellStyle(fillPattern = CellFill.Solid, fillForegroundColor = Color.AquaMarine, font = Font(bold = true))

  def childSheet: Future[Sheet] = {
    childRepository.findAll.map { children =>
      val rows = children.map { child =>
        Row().withCellValues(
          child.id,
          child.entity.firstName,
          child.entity.lastName,
          child.entity.birthDate.map(_.toString).getOrElse(""),
          child.entity.address.street.getOrElse(""),
          child.entity.address.number.getOrElse(""),
          child.entity.address.zipCode.map(_.toString).getOrElse(""),
          child.entity.address.city.getOrElse(""),
          child.entity.contact.email.mkString(", "),
          child.entity.contact.phone.map { phoneContact =>
            phoneContact.phoneNumber + phoneContact.comment.map(x => s" ($x)").getOrElse("") +
              phoneContact.kind.map(x => s" ($x)").getOrElse("")
          }.mkString(", ")
        )
      }

      Sheet(name = "Alle kinderen")
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
            )
          ) ++ rows
        )
        .withColumns(
          (0 to 9).map(idx => Column(index = idx, autoSized = true)): _*
        )
    }
  }

  def crewSheet: Future[Sheet] = {
    crewRepository.findAll map { crew =>
      val rows = crew.map { crewMember =>
        Row().withCellValues(
          crewMember.id,
          crewMember.entity.firstName,
          crewMember.entity.lastName,
          crewMember.entity.birthDate.map(_.toString).getOrElse(""),
          crewMember.entity.yearStarted.map(_.toString).getOrElse(""),
          crewMember.entity.address.street.getOrElse(""),
          crewMember.entity.address.number.getOrElse(""),
          crewMember.entity.address.zipCode.map(_.toString).getOrElse(""),
          crewMember.entity.address.city.getOrElse(""),
          crewMember.entity.bankAccount.getOrElse(""),
          crewMember.entity.contact.email.mkString("\n"),
          crewMember.entity.contact.phone.map { phoneContact =>
            phoneContact.phoneNumber + phoneContact.comment.map(x => s" ($x)").getOrElse("") +
              phoneContact.kind.map(x => s" ($x)").getOrElse("")
          }.mkString(", ")
        )
      }

      Sheet(name = "Alle animatoren")
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
            )
          ) ++ rows
        )
        .withColumns(
          (0 to 11).map(idx => Column(index = idx, autoSized = true)): _*
        )
    }
  }

}
