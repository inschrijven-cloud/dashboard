package be.thomastoye.speelsysteem.dashboard.controllers.api

import java.io.File
import javax.inject.Inject

import be.thomastoye.speelsysteem.dashboard.controllers.actions.DomainAction
import be.thomastoye.speelsysteem.data.{ FiscalCertificateService, ReportService }
import play.api.mvc.{ Action, AnyContent, InjectedController }
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext

class ReportController @Inject() (
    fiscalCertificateService: FiscalCertificateService,
    reportService: ReportService,
    domainAction: DomainAction
)(implicit ec: ExecutionContext) extends InjectedController with StrictLogging {

  def downloadFiscalCertificates(year: Int): Action[AnyContent] = (Action andThen domainAction).async { req =>
    fiscalCertificateService.getFiscalCertificateSheet(year)(req.tenant) map { sheet =>
      val file = File.createTempFile("fiscale-attesten.xlsx", System.nanoTime().toString)
      sheet.saveAsXlsx(file.getAbsolutePath)

      Ok.sendFile(file, fileName = _ => "fiscale-attesten.xlsx")
    }
  }

  def downloadFiscalCertificateForChild(year: Int, childId: String): Action[AnyContent] = TODO

  def downloadCompensation(year: Int): Action[AnyContent] = TODO

  def downloadCompensationForCrew(year: Int, crewId: String): Action[AnyContent] = TODO

  def downloadChildrenPerDay(year: Int): Action[AnyContent] = (Action andThen domainAction).async { req =>
    reportService.getChildrenPerDay(year)(req.tenant) map { sheet =>
      val file = File.createTempFile(s"kinderen per dag - $year.xlsx", System.nanoTime().toString)
      sheet.saveAsXlsx(file.getAbsolutePath)

      Ok.sendFile(file, fileName = _ => s"kinderen per dag - $year.xlsx")
    }
  }

}
