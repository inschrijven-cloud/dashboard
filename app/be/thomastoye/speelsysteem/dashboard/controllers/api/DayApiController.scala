package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.data.{ChildRepository, DayService}
import be.thomastoye.speelsysteem.models.JsonFormats.dayWithIdWrites
import be.thomastoye.speelsysteem.models.Shift.Id
import be.thomastoye.speelsysteem.models.{Child, Day, Shift}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc._

class DayApiController @Inject() (dayService: DayService, childRepository: ChildRepository) extends ApiController {

  def all = Action.async { req => dayService.findAll.map(days => Ok(Json.toJson(days))) }

  def create = TODO

  def getById(id: Day.Id) = TODO

  def update = TODO
}
