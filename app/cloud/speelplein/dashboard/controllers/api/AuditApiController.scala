package cloud.speelplein.dashboard.controllers.api

import cloud.speelplein.dashboard.controllers.actions.{
  AuditLoggingRequest,
  LoggingVerifyingBuilder,
  TenantAction
}
import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.data.AuditLogService
import cloud.speelplein.models.JsonFormats.auditLogEntryFormat
import cloud.speelplein.models.AuditLogData
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, ActionBuilder, AnyContent}

import scala.concurrent.ExecutionContext

class AuditApiController @Inject()(
    tenantAction: TenantAction,
    auditAuthorizationBuilder: LoggingVerifyingBuilder,
    auditLogService: AuditLogService,
    implicit val ec: ExecutionContext
) extends ApiController {

  private def action(
      perm: Permission,
      data: AuditLogData): ActionBuilder[AuditLoggingRequest, AnyContent] =
    Action andThen tenantAction andThen auditAuthorizationBuilder.logAndVerify(
      perm,
      data)

  private def action(
      perm: Permission): ActionBuilder[AuditLoggingRequest, AnyContent] =
    action(perm, AuditLogData.empty)

  def add: Action[AnyContent] = action(Permission.auditLogAddEntry).async {
    req =>
      ???
  }

  def getLog(offset: Int, count: Int): Action[AnyContent] =
    action(Permission.auditLogRead).async { req =>
      auditLogService.getLogData(count, offset)(req.tenant) map { data =>
        Ok(Json.toJson(data))
      }
    }
}
