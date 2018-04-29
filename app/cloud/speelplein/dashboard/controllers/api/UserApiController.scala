package cloud.speelplein.dashboard.controllers.api

import javax.inject.Inject
import cloud.speelplein.dashboard.controllers.actions._
import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.dashboard.controllers.api.auth.Permission.{
  userPutTenantData,
  userPutTenantDataAnyTenant,
  userRetrieve
}
import cloud.speelplein.data.UserService
import cloud.speelplein.models.{AuditLogData, Tenant}
import cloud.speelplein.models.JsonFormats.defaultUserWrites
import play.api.libs.json.Json
import play.api.mvc.{Action, ActionBuilder, AnyContent}

import scala.concurrent.ExecutionContext

class UserApiController @Inject()(
    userService: UserService,
    tenantAction: TenantAction,
    globalTenantOnlyAction: GlobalTenantOnlyAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder,
    auditAuthorizationBuilder: LoggingVerifyingBuilder,
    implicit val ec: ExecutionContext)
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

  case class RolesAndPermissions(roles: Seq[String], permissions: Seq[String])
  implicit private val rolesAndPermissionsFormat =
    Json.format[RolesAndPermissions]

  def getAll: Action[AnyContent] = action(userRetrieve).async { req =>
    userService.getAll map { all =>
      Ok(Json.toJson(all))
    }
  }

  def getById(id: String): Action[AnyContent] =
    action(userRetrieve, AuditLogData.userId(id)).async { req =>
      userService.getById(id) map { maybeUser =>
        maybeUser map (user => Ok(Json.toJson(user))) getOrElse NotFound
      }
    }

  def getTenantData(userId: String): Action[AnyContent] =
    action(userRetrieve, AuditLogData.userId(userId)).async { req =>
      userService.getById(userId) map { user =>
        // TODO should probably filter out other tenants and only show data for current tenant
        Ok(Json.toJson("TODO"))
      }
    }

  def putTenantData(userId: String,
                    tenant: String): Action[RolesAndPermissions] =
    action(userPutTenantDataAnyTenant,
           AuditLogData.userId(userId).copy(tenantName = Some(tenant)))
      .async(parse.json[RolesAndPermissions]) { req =>
        userService.setRolesAndPermissionsForUser(userId,
                                                  Tenant.apply(tenant),
                                                  req.body.roles,
                                                  req.body.permissions) map {
          case Some(_) => updated(userId)
          case _       => NotFound
        }
      }

  def putTenantData(userId: String): Action[RolesAndPermissions] =
    action(userPutTenantData, AuditLogData.userId(userId))
      .async(parse.json[RolesAndPermissions]) { req =>
        userService.setRolesAndPermissionsForUser(userId,
                                                  req.tenant,
                                                  req.body.roles,
                                                  req.body.permissions) map {
          result =>
            Ok(Json.toJson("TODO"))
        }
      }
}
