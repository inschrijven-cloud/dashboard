package cloud.speelplein.dashboard.controllers.api

import javax.inject.Inject

import cloud.speelplein.dashboard.controllers.actions.{ DomainAction, GlobalDomainOnlyAction, JwtAuthorizationBuilder }
import cloud.speelplein.dashboard.controllers.api.auth.Permission.listDatabases
import cloud.speelplein.models.JsonFormats.dbNameWrites
import cloud.speelplein.dashboard.controllers.actions.{ DomainAction, GlobalDomainOnlyAction, JwtAuthorizationBuilder }
import cloud.speelplein.data.TenantDatabaseService
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent }

import scala.concurrent.{ ExecutionContext, Future }

class DatabaseController @Inject() (
    domainAction: DomainAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder,
    databaseService: TenantDatabaseService,
    globalDomainOnlyAction: GlobalDomainOnlyAction
)(implicit ec: ExecutionContext) extends ApiController {

  def list: Action[AnyContent] = (Action
    andThen domainAction
    andThen globalDomainOnlyAction
    andThen jwtAuthorizationBuilder.authenticate(listDatabases)).async { req =>
      databaseService.all map { dbs => Ok(Json.toJson(dbs)) }
    }
}
