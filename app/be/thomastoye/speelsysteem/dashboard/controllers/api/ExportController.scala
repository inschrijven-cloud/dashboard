package be.thomastoye.speelsysteem.dashboard.controllers.api

import java.io.File
import java.time.LocalDate
import javax.inject.Inject

import be.thomastoye.speelsysteem.data.ExportService
import be.thomastoye.speelsysteem.models.DayDate
import play.api.mvc.{ Action, AnyContent, Controller }
import play.api.libs.concurrent.Execution.Implicits._
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._

class ExportController @Inject() (exportService: ExportService) extends Controller {
  def downloadChildren: Action[AnyContent] = Action.async { req =>
    exportService.childSheet map { sheet =>
      val file = File.createTempFile("kinderen.xlsx", System.nanoTime().toString)
      sheet.saveAsXlsx(file.getAbsolutePath)

      Ok.sendFile(file, fileName = _ => "kinderen " + DayDate.createFromLocalDate(LocalDate.now).getDayId + ".xlsx")
    }
  }

  def downloadCrew: Action[AnyContent] = Action.async { req =>
    exportService.crewSheet map { sheet =>
      val file = File.createTempFile("animatoren.xlsx", System.nanoTime().toString)
      sheet.saveAsXlsx(file.getAbsolutePath)

      Ok.sendFile(file, fileName = _ => "animatoren " + DayDate.createFromLocalDate(LocalDate.now).getDayId + ".xlsx")
    }
  }

  def downloadChildrenWitbExtraMedicalAttention: Action[AnyContent] = TODO
}
