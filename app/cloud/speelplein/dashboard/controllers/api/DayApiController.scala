package cloud.speelplein.dashboard.controllers.api

import javax.inject.Inject
import cloud.speelplein.dashboard.controllers.actions.{
  AuditLoggingRequest,
  LoggingVerifyingBuilder,
  TenantAction
}
import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.dashboard.controllers.api.auth.Permission._
import cloud.speelplein.data.{ChildRepository, DayService}
import cloud.speelplein.models.{AuditLogData, Day}
import cloud.speelplein.models.JsonFormats.{dayFormat, dayWithIdWrites}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class DayApiController @Inject()(
    dayService: DayService,
    childRepository: ChildRepository,
    tenantAction: TenantAction,
    auditAuthorizationBuilder: LoggingVerifyingBuilder
)(implicit ec: ExecutionContext)
    extends ApiController {
  private def action(
      perm: Permission,
      data: AuditLogData): ActionBuilder[AuditLoggingRequest, AnyContent] =
    Action andThen tenantAction andThen auditAuthorizationBuilder.logAndVerify(
      perm,
      data)

  private def action(
      perm: Permission): ActionBuilder[AuditLoggingRequest, AnyContent] =
    action(perm, AuditLogData.empty)

  def all: Action[AnyContent] = action(dayRetrieve).async { req =>
    dayService.findAll(req.tenant).map(days => Ok(Json.toJson(days)))
  }

  def create: Action[Day] = action(dayCreate).async(parse.json(dayFormat)) {
    req =>
      dayService.insert(req.body)(req.tenant).map(_ => Ok)
  }

  def getById(id: Day.Id): Action[AnyContent] =
    action(dayRetrieve, AuditLogData.dayId(id)).async { req =>
      dayService.findById(id)(req.tenant).map { maybeDay =>
        maybeDay
          .map(dayWithId => Ok(Json.toJson(dayWithId)))
          .getOrElse(NotFound)
      }
    }

  def update(id: Day.Id): Action[Day] =
    action(dayUpdate, AuditLogData.dayId(id)).async(parse.json(dayFormat)) {
      req =>
        dayService.update(id, req.body)(req.tenant).map(_ => Ok)
    }
}
