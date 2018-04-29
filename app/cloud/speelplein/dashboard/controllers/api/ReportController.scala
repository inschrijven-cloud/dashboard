package cloud.speelplein.dashboard.controllers.api

import java.io.File

import javax.inject.Inject
import cloud.speelplein.dashboard.controllers.actions.{
  AuditLoggingRequest,
  JwtAuthorizationBuilder,
  LoggingVerifyingBuilder,
  TenantAction
}
import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.dashboard.controllers.api.auth.Permission._
import cloud.speelplein.data.{FiscalCertificateService, ReportService}
import cloud.speelplein.models.AuditLogData
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._
import com.typesafe.scalalogging.StrictLogging
import play.api.mvc.{Action, ActionBuilder, AnyContent, InjectedController}

import scala.concurrent.ExecutionContext

class ReportController @Inject()(
    fiscalCertificateService: FiscalCertificateService,
    reportService: ReportService,
    tenantAction: TenantAction,
    auditAuthorizationBuilder: LoggingVerifyingBuilder
)(implicit ec: ExecutionContext)
    extends InjectedController
    with StrictLogging {

  private def action(
      perm: Permission,
      data: AuditLogData): ActionBuilder[AuditLoggingRequest, AnyContent] =
    Action andThen tenantAction andThen auditAuthorizationBuilder.logAndVerify(
      perm,
      data)

  private def action(
      perm: Permission): ActionBuilder[AuditLoggingRequest, AnyContent] =
    action(perm, AuditLogData.empty)

  def downloadFiscalCertificates(year: Int): Action[AnyContent] =
    action(exportFiscalCert, AuditLogData.year(year)).async { req =>
      fiscalCertificateService.getFiscalCertificateSheet(year)(req.tenant) map {
        sheet =>
          val file = File.createTempFile("fiscale-attesten.xlsx",
                                         System.nanoTime().toString)
          sheet.saveAsXlsx(file.getAbsolutePath)

          Ok.sendFile(file, fileName = _ => "fiscale-attesten.xlsx")
      }
    }

  def downloadFiscalCertificateForChild(year: Int,
                                        childId: String): Action[AnyContent] =
    TODO

  def downloadCompensation(year: Int): Action[AnyContent] = TODO

  def downloadCompensationForCrew(year: Int,
                                  crewId: String): Action[AnyContent] = TODO

  def downloadChildrenPerDay(year: Int): Action[AnyContent] =
    action(exportChildrenPerDay, AuditLogData.year(year)).async { req =>
      reportService.getChildrenPerDay(year)(req.tenant) map { sheet =>
        val file = File.createTempFile(s"kinderen per dag - $year.xlsx",
                                       System.nanoTime().toString)
        sheet.saveAsXlsx(file.getAbsolutePath)

        Ok.sendFile(file, fileName = _ => s"kinderen per dag - $year.xlsx")
      }
    }

}
