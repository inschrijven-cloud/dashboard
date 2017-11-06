package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.dashboard.controllers.actions.{ DomainAction, JwtAuthorizationBuilder }
import be.thomastoye.speelsysteem.dashboard.controllers.api.auth.Permission
import be.thomastoye.speelsysteem.dashboard.controllers.api.auth.Permission.permissionFormat
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class AuthController @Inject() (domainAction: DomainAction)(implicit ec: ExecutionContext) extends ApiController {
  def allPermissions = (Action andThen domainAction) { req => Ok(Json.toJson(Permission.all)) }
}
