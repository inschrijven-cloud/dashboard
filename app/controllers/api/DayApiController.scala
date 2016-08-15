package controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.data.ChildRepository
import be.thomastoye.speelsysteem.data.couchdb.CouchDayService
import be.thomastoye.speelsysteem.models.{Child, Day, Shift}
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import be.thomastoye.speelsysteem.models.JsonFormats.{dayFormat, dayWithIdWrites}

class DayApiController @Inject() (couchDayService: CouchDayService, childRepository: ChildRepository) extends Controller {
  case class BindShiftIds(shiftIds: Seq[Shift.Id])
  val bindShiftIdsReads = Json.reads[BindShiftIds]

  def all = Action.async { req => couchDayService.findAll.map(days => Ok(Json.toJson(days))) }

  def getAttendancesForChild(id: Child.Id) = Action.async { req =>
    couchDayService.findAttendancesForChild(id).map(att => Ok(Json.toJson(att)))
  }

  def addAttendancesForChild(childId: Child.Id, dayId: Day.Id) = Action.async(BodyParsers.parse.json(bindShiftIdsReads)) { req =>
    childRepository.addAttendancesForChild(childId, dayId, req.body.shiftIds) map { foundOpt =>
      foundOpt.map(_ => Ok).getOrElse(NotFound)
    }
  }
}
