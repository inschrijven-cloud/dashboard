package be.thomastoye.speelsysteem.dashboard.controllers.api

import java.io.File
import java.time.LocalDate
import javax.inject.Inject

import be.thomastoye.speelsysteem.data.ExportService
import be.thomastoye.speelsysteem.models.DayDate
import play.api.mvc._
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._

import scala.concurrent.ExecutionContext

class ExportController @Inject() (exportService: ExportService, cc: ControllerComponents)(implicit ec: ExecutionContext) extends InjectedController {
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
