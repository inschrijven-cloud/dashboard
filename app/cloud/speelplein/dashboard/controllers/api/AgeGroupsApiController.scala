package cloud.speelplein.dashboard.controllers.api

import cloud.speelplein.dashboard.controllers.actions.{
  AuditLoggingRequest,
  LoggingVerifyingBuilder,
  TenantAction
}
import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.data.AgeGroupService
import cloud.speelplein.models.{AgeGroupConfig, AuditLogData}
import cloud.speelplein.models.JsonFormats.ageGroupConfigFormat
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, ActionBuilder, AnyContent}

import scala.concurrent.ExecutionContext

class AgeGroupsApiController @Inject()(
    ageGroupService: AgeGroupService,
    tenantAction: TenantAction,
    auditAuthorizationBuilder: LoggingVerifyingBuilder,
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

  def getAll: Action[AnyContent] = action(Permission.ageGroupsRead).async {
    req =>
      ageGroupService.get()(req.tenant).map { ageGroups =>
        Ok(Json.toJson(ageGroups))
      }
  }

  def createOrUpdate: Action[AgeGroupConfig] =
    action(Permission.ageGroupsCreateAndUpdate)
      .async(parse.json(ageGroupConfigFormat)) { req =>
        ageGroupService.updateOrCreate(req.body)(req.tenant) map { _ =>
          Created(Json.toJson(Json.obj("status" -> "created")))
        }
      }
}
