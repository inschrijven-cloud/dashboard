package controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.data.couchdb.CouchDayService
import be.thomastoye.speelsysteem.models.Child
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import be.thomastoye.speelsysteem.models.JsonFormats.{dayWithIdWrites, dayFormat}

class DayApiController @Inject() (couchDayService: CouchDayService) extends Controller {

  def all = Action.async { req => couchDayService.findAll.map(days => Ok(Json.toJson(days))) }

  def getAttendancesForChild(id: Child.Id) = Action.async { req =>
    couchDayService.findAttendancesForChild(id).map(att => Ok(Json.toJson(att)))
  }

}
