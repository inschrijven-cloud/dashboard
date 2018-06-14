package cloud.speelplein.dashboard.controllers.api

import java.io.File
import java.time.LocalDate

import javax.inject.Inject
import cloud.speelplein.dashboard.controllers.actions.{
  AuditLoggingRequest,
  JwtAuthorizationBuilder,
  LoggingVerifyingBuilder,
  TenantAction
}
import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.dashboard.controllers.api.auth.Permission._
import cloud.speelplein.data.ExportService
import cloud.speelplein.models.{AuditLogData, Child, Day, DayDate}
import ExportController.{BindChildIds, bindChildIdsFormat}
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._
import play.api.libs.json.{Json, OFormat}
import play.api.mvc._

import scala.concurrent.ExecutionContext

object ExportController {
  case class BindChildIds(childIds: Seq[Child.Id])
  implicit val bindChildIdsFormat: OFormat[BindChildIds] =
    Json.format[BindChildIds]
}

class ExportController @Inject()(
    exportService: ExportService,
    cc: ControllerComponents,
    tenantAction: TenantAction,
    auditAuthorizationBuilder: LoggingVerifyingBuilder
)(implicit ec: ExecutionContext)
    extends InjectedController {

  private def action(
      perm: Permission,
      data: AuditLogData): ActionBuilder[AuditLoggingRequest, AnyContent] =
    Action andThen tenantAction andThen auditAuthorizationBuilder.logAndVerify(
      perm,
      data)

  private def action(
      perm: Permission): ActionBuilder[AuditLoggingRequest, AnyContent] =
    action(perm, AuditLogData.empty)

  def downloadChildren: Action[AnyContent] = action(exportChildren).async {
    req =>
      exportService.childSheet(req.tenant) map { sheet =>
        val file =
          File.createTempFile("kinderen.xlsx", System.nanoTime().toString)
        sheet.saveAsXlsx(file.getAbsolutePath)

        Ok.sendFile(file,
                    fileName = _ =>
                      "kinderen " + DayDate
                        .createFromLocalDate(LocalDate.now)
                        .getDayId + ".xlsx")
      }
  }

  def downloadCrew: Action[AnyContent] = action(exportCrew).async { req =>
    exportService.crewSheet(req.tenant) map { sheet =>
      val file =
        File.createTempFile("animatoren.xlsx", System.nanoTime().toString)
      sheet.saveAsXlsx(file.getAbsolutePath)

      Ok.sendFile(file,
                  fileName = _ =>
                    "animatoren " + DayDate
                      .createFromLocalDate(LocalDate.now)
                      .getDayId + ".xlsx")
    }
  }

  def downloadChildrenWithRemarks: Action[AnyContent] =
    action(exportChildren).async { req =>
      exportService.childrenWithRemarksSheet(req.tenant) map { sheet =>
        val file =
          File.createTempFile("kinderen met opmerkingen.xlsx",
                              System.nanoTime().toString)
        sheet.saveAsXlsx(file.getAbsolutePath)

        Ok.sendFile(file,
                    fileName = _ =>
                      "kinderen " + DayDate
                        .createFromLocalDate(LocalDate.now)
                        .getDayId + ".xlsx")
      }
    }
}
