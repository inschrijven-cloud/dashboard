package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.data.{ ChildRepository, DayService }
import be.thomastoye.speelsysteem.models.JsonFormats.{ dayFormat, dayWithIdWrites }
import be.thomastoye.speelsysteem.models.Day
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class DayApiController @Inject() (dayService: DayService, childRepository: ChildRepository)(implicit ec: ExecutionContext) extends ApiController {

  def all: Action[AnyContent] = Action.async { req =>
    dayService.findAll.map(days => Ok(Json.toJson(days)))
  }

  def create: Action[Day] = Action.async(parse.json(dayFormat)) { req =>
    dayService.insert(req.body).map(_ => Ok)
  }

  def getById(id: Day.Id): Action[AnyContent] = Action.async { req =>
    dayService.findById(id).map { maybeDay =>
      maybeDay.map(dayWithId => Ok(Json.toJson(dayWithId))).getOrElse(NotFound)
    }
  }

  def update(id: Day.Id): Action[Day] = Action.async(parse.json(dayFormat)) { req =>
    dayService.update(id, req.body).map(_ => Ok)
  }
}
