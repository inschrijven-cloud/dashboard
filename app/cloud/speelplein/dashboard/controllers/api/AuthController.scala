package cloud.speelplein.dashboard.controllers.api

import javax.inject.Inject

import cloud.speelplein.dashboard.controllers.api.auth.{Permission, Role}
import cloud.speelplein.dashboard.controllers.api.auth.Permission.permissionFormat
import cloud.speelplein.dashboard.controllers.api.auth.Role.roleFormat
import cloud.speelplein.dashboard.controllers.actions.DomainAction
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class AuthController @Inject()(domainAction: DomainAction)(
    implicit ec: ExecutionContext)
    extends ApiController {
  def allPermissions: Action[AnyContent] = (Action andThen domainAction) {
    req =>
      Ok(Json.toJson(Permission.all.map {
        case (categoryName, values) =>
          Json.obj("categoryName" -> categoryName, "permissions" -> values)
      }))
  }

  def allRoles: Action[AnyContent] = (Action andThen domainAction) { req =>
    Ok(Json.toJson(Role.all map {
      case (levelName, value) =>
        Json.obj("level" -> levelName, "roles" -> value)
    }))
  }
}
