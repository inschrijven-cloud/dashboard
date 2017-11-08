package cloud.speelplein.dashboard.controllers.api

import java.io.File
import javax.inject.Inject

import cloud.speelplein.dashboard.controllers.actions.{
  DomainAction,
  JwtAuthorizationBuilder
}
import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.dashboard.controllers.api.auth.Permission._
import cloud.speelplein.data.{FiscalCertificateService, ReportService}
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._
import com.typesafe.scalalogging.StrictLogging
import play.api.mvc.{Action, AnyContent, InjectedController}

import scala.concurrent.ExecutionContext

class ReportController @Inject()(
    fiscalCertificateService: FiscalCertificateService,
    reportService: ReportService,
    domainAction: DomainAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder
)(implicit ec: ExecutionContext)
    extends InjectedController
    with StrictLogging {
  private def action(per: Permission) =
    Action andThen domainAction andThen jwtAuthorizationBuilder.authenticate(
      per)

  def downloadFiscalCertificates(year: Int): Action[AnyContent] =
    action(exportFiscalCert).async { req =>
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
    action(exportChildrenPerDay).async { req =>
      reportService.getChildrenPerDay(year)(req.tenant) map { sheet =>
        val file = File.createTempFile(s"kinderen per dag - $year.xlsx",
                                       System.nanoTime().toString)
        sheet.saveAsXlsx(file.getAbsolutePath)

        Ok.sendFile(file, fileName = _ => s"kinderen per dag - $year.xlsx")
      }
    }

}
