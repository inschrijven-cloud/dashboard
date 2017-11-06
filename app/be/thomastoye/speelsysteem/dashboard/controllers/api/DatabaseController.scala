package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.dashboard.controllers.actions.{ DomainAction, JwtAuthorizationBuilder }
import be.thomastoye.speelsysteem.dashboard.services.TenantDatabaseService
import be.thomastoye.speelsysteem.dashboard.controllers.api.auth.Permission.listDatabases
import be.thomastoye.speelsysteem.models.JsonFormats.dbNameWrites
import play.api.libs.json.Json

import scala.concurrent.{ ExecutionContext, Future }

class DatabaseController @Inject() (
    domainAction: DomainAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder,
    databaseService: TenantDatabaseService
)(implicit ec: ExecutionContext) extends ApiController {

  def list = (Action andThen domainAction andThen jwtAuthorizationBuilder.authenticate(listDatabases)).async { req =>
    if (req.tenant.name == "global") {
      databaseService.all map { dbs => Ok(Json.toJson(dbs)) }
    } else {
      Future.successful(Unauthorized(Json.obj("status" -> "error", "reason" -> "This route is only availaible on the tenant 'global'.")))
    }
  }
}
