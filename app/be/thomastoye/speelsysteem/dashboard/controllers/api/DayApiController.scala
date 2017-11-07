package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.dashboard.controllers.actions.{DomainAction, JwtAuthorizationBuilder}
import be.thomastoye.speelsysteem.dashboard.controllers.api.auth.Permission
import be.thomastoye.speelsysteem.dashboard.controllers.api.auth.Permission._
import be.thomastoye.speelsysteem.data.{ChildRepository, DayService}
import be.thomastoye.speelsysteem.models.JsonFormats.{dayFormat, dayWithIdWrites}
import be.thomastoye.speelsysteem.models.Day
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class DayApiController @Inject() (
    dayService: DayService,
    childRepository: ChildRepository,
    domainAction: DomainAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder
)(implicit ec: ExecutionContext) extends ApiController {
  private def action(per: Permission) = Action andThen domainAction andThen jwtAuthorizationBuilder.authenticate(dayRetrieve)

  def all: Action[AnyContent] = action(dayRetrieve).async { req =>
    dayService.findAll(req.tenant).map(days => Ok(Json.toJson(days)))
  }

  def create: Action[Day] = action(dayCreate).async(parse.json(dayFormat)) { req =>
    dayService.insert(req.body)(req.tenant).map(_ => Ok)
  }

  def getById(id: Day.Id): Action[AnyContent] = action(dayRetrieve).async { req =>
    dayService.findById(id)(req.tenant).map { maybeDay =>
      maybeDay.map(dayWithId => Ok(Json.toJson(dayWithId))).getOrElse(NotFound)
    }
  }

  def update(id: Day.Id): Action[Day] = action(dayUpdate).async(parse.json(dayFormat)) { req =>
    dayService.update(id, req.body)(req.tenant).map(_ => Ok)
  }
}
