package be.thomastoye.speelsysteem.dashboard.controllers.api

import java.io.File
import javax.inject.Inject

import be.thomastoye.speelsysteem.data.FiscalCertificateService
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits._
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._
import com.typesafe.scalalogging.StrictLogging

class ReportController @Inject()(fiscalCertificateService: FiscalCertificateService) extends Controller with StrictLogging {

  def downloadFiscalCertificates(year: Int) = Action.async {
   fiscalCertificateService.getFiscalCertificateSheet(year) map { sheet =>
      val file = File.createTempFile("fiscale-attesten.xlsx", System.nanoTime().toString)
      sheet.saveAsXlsx(file.getAbsolutePath)

      Ok.sendFile(file, fileName = _ => "fiscale-attesten.xlsx")
    }
  }

  def downloadFiscalCertificateForChild(year: Int, childId: String) = TODO

  def downloadCompensation(year: Int) = TODO

  def downloadCompensationForCrew(year: Int, crewId: String) = TODO

}
