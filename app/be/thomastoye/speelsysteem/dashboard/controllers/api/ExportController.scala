package be.thomastoye.speelsysteem.dashboard.controllers.api

import java.io.File
import java.time.LocalDate
import javax.inject.Inject

import be.thomastoye.speelsysteem.dashboard.controllers.actions.{DomainAction, JwtAuthorizationBuilder}
import be.thomastoye.speelsysteem.dashboard.controllers.api.auth.Permission
import be.thomastoye.speelsysteem.dashboard.controllers.api.auth.Permission._
import be.thomastoye.speelsysteem.data.ExportService
import be.thomastoye.speelsysteem.models.DayDate
import play.api.mvc._
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._

import scala.concurrent.ExecutionContext

class ExportController @Inject() (
    exportService: ExportService,
    cc: ControllerComponents,
    domainAction: DomainAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder
)(implicit ec: ExecutionContext) extends InjectedController {
  private def action(per: Permission) = Action andThen domainAction andThen jwtAuthorizationBuilder.authenticate(per)

  def downloadChildren: Action[AnyContent] = action(exportChildren).async { req =>
    exportService.childSheet(req.tenant) map { sheet =>
      val file = File.createTempFile("kinderen.xlsx", System.nanoTime().toString)
      sheet.saveAsXlsx(file.getAbsolutePath)

      Ok.sendFile(file, fileName = _ => "kinderen " + DayDate.createFromLocalDate(LocalDate.now).getDayId + ".xlsx")
    }
  }

  def downloadCrew: Action[AnyContent] = action(exportCrew).async { req =>
    exportService.crewSheet(req.tenant) map { sheet =>
      val file = File.createTempFile("animatoren.xlsx", System.nanoTime().toString)
      sheet.saveAsXlsx(file.getAbsolutePath)

      Ok.sendFile(file, fileName = _ => "animatoren " + DayDate.createFromLocalDate(LocalDate.now).getDayId + ".xlsx")
    }
  }

  def downloadChildrenWitbExtraMedicalAttention: Action[AnyContent] = TODO
}
