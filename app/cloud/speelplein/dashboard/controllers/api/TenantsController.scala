package cloud.speelplein.dashboard.controllers.api

import javax.inject.Inject

import cloud.speelplein.dashboard.controllers.actions.{
  TenantAction,
  GlobalTenantOnlyAction,
  JwtAuthorizationBuilder
}
import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.dashboard.controllers.api.auth.Permission._
import cloud.speelplein.data.TenantsService
import cloud.speelplein.data.couchdb.RemoteDbConfigImpl
import cloud.speelplein.models.JsonFormats.tenantFormat
import cloud.speelplein.models.Tenant
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.{ExecutionContext, Future}

class TenantsController @Inject()(
    tenantAction: TenantAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder,
    globalTenantOnlyAction: GlobalTenantOnlyAction,
    tenantsService: TenantsService,
    remoteCouchDB: RemoteDbConfigImpl
)(implicit ec: ExecutionContext)
    extends ApiController {
  private def action(per: Permission) =
    Action andThen tenantAction andThen globalTenantOnlyAction andThen jwtAuthorizationBuilder
      .authenticate(per)

  def list: Action[AnyContent] = action(listTenants).async { req =>
    tenantsService.all.map(all => Ok(Json.toJson(all)))
  }

  def create(name: String): Action[AnyContent] =
    action(createTenant).async { req =>
      if (Tenant.isValidNewTenantName(name)) {
        tenantsService
          .create(Tenant(name))
          .map(_ => created(name))
      } else
        Future.successful(
          BadRequest(
            Json.obj("status" -> "failed",
                     "reason" -> "Tenant name contains invalid characters")))
    }

  def details(tenant: String): Action[AnyContent] = TODO

  def generateDesignDocs(tenant: String): Action[AnyContent] =
    action(initTenantDbs).async { req =>
      tenantsService.initializeDatabase(req.tenant).map(res => Ok(res))
    }

  def syncTo(tenant: String): Action[AnyContent] = action(syncTenantDb).async {
    req =>
      tenantsService.syncTo(Tenant(tenant), remoteCouchDB).map(res => Ok(res))
  }

  def syncFrom(tenant: String): Action[AnyContent] =
    action(syncTenantDb).async { req =>
      tenantsService.syncFrom(Tenant(tenant), remoteCouchDB).map(res => Ok(res))
    }
}
