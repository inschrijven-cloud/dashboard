package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.dashboard.controllers.actions.{DomainAction, GlobalDomainOnlyAction, JwtAuthorizationBuilder}
import be.thomastoye.speelsysteem.dashboard.controllers.api.auth.Permission.listDatabases
import be.thomastoye.speelsysteem.data.TenantDatabaseService
import be.thomastoye.speelsysteem.models.JsonFormats.dbNameWrites
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.{ExecutionContext, Future}

class DatabaseController @Inject() (
    domainAction: DomainAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder,
    databaseService: TenantDatabaseService,
    globalDomainOnlyAction: GlobalDomainOnlyAction
)(implicit ec: ExecutionContext) extends ApiController {

  def list: Action[AnyContent] = (Action
    andThen domainAction
    andThen globalDomainOnlyAction
    andThen jwtAuthorizationBuilder.authenticate(listDatabases)).async
  { req =>
    databaseService.all map { dbs => Ok(Json.toJson(dbs)) }
  }
}
