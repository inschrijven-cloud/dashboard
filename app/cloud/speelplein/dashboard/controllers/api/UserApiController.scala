package cloud.speelplein.dashboard.controllers.api

import javax.inject.Inject

import cloud.speelplein.dashboard.controllers.actions.{
  TenantAction,
  GlobalTenantOnlyAction,
  JwtAuthorizationBuilder
}
import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.dashboard.controllers.api.auth.Permission.{
  userPutTenantData,
  userPutTenantDataAnyTenant,
  userRetrieve
}
import cloud.speelplein.data.UserService
import cloud.speelplein.models.Tenant
import cloud.speelplein.models.JsonFormats.defaultUserWrites
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.ExecutionContext

class UserApiController @Inject()(
    userService: UserService,
    tenantAction: TenantAction,
    globalTenantOnlyAction: GlobalTenantOnlyAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder,
    implicit val ec: ExecutionContext)
    extends ApiController {

  private def action(per: Permission) =
    Action andThen tenantAction andThen globalTenantOnlyAction andThen jwtAuthorizationBuilder
      .authenticate(per)

  case class RolesAndPermissions(roles: Seq[String], permissions: Seq[String])
  implicit private val rolesAndPermissionsFormat =
    Json.format[RolesAndPermissions]

  def getAll: Action[AnyContent] = action(userRetrieve).async { req =>
    userService.getAll map { all =>
      Ok(Json.toJson(all))
    }
  }

  def getById(id: String): Action[AnyContent] = action(userRetrieve).async {
    req =>
      userService.getById(id) map { maybeUser =>
        maybeUser map (user => Ok(Json.toJson(user))) getOrElse NotFound
      }
  }

  def getTenantData(userId: String): Action[AnyContent] =
    action(userRetrieve).async { req =>
      userService.getById(userId) map { user =>
        // TODO should probably filter out other tenants and only show data for current tenant
        Ok(Json.toJson("TODO"))
      }
    }

  def putTenantData(userId: String,
                    tenant: String): Action[RolesAndPermissions] =
    action(userPutTenantDataAnyTenant).async(parse.json[RolesAndPermissions]) {
      req =>
        userService.setRolesAndPermissionsForUser(userId,
                                                  Tenant.apply(tenant),
                                                  req.body.roles,
                                                  req.body.permissions) map {
          case Some(_) => updated(userId)
          case _       => NotFound
        }
    }

  def putTenantData(userId: String): Action[RolesAndPermissions] =
    action(userPutTenantData).async(parse.json[RolesAndPermissions]) { req =>
      userService.setRolesAndPermissionsForUser(userId,
                                                req.tenant,
                                                req.body.roles,
                                                req.body.permissions) map {
        result =>
          Ok(Json.toJson("TODO"))
      }
    }
}
