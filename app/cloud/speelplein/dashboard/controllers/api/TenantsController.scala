package cloud.speelplein.dashboard.controllers.api

import javax.inject.Inject

import cloud.speelplein.dashboard.controllers.actions.{
  DomainAction,
  GlobalDomainOnlyAction,
  JwtAuthorizationBuilder
}
import cloud.speelplein.dashboard.controllers.api.TenantsController.TenantBinder
import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.dashboard.controllers.api.auth.Permission._
import cloud.speelplein.data.TenantsService
import cloud.speelplein.data.couchdb.RemoteDbConfigImp
import cloud.speelplein.models.JsonFormats.tenantFormat
import cloud.speelplein.models.Tenant
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.{ExecutionContext, Future}

object TenantsController {
  case class TenantBinder(name: String)
  implicit val tenantBinderFormat: OFormat[TenantBinder] =
    Json.format[TenantBinder]
}

class TenantsController @Inject()(
    domainAction: DomainAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder,
    globalDomainOnlyAction: GlobalDomainOnlyAction,
    tenantsService: TenantsService,
    remoteCouchDB: RemoteDbConfigImp
)(implicit ec: ExecutionContext)
    extends ApiController {
  private def action(per: Permission) =
    Action andThen domainAction andThen globalDomainOnlyAction andThen jwtAuthorizationBuilder
      .authenticate(listTenants)

  def list: Action[AnyContent] = action(listTenants).async { req =>
    tenantsService.all.map(all => Ok(Json.toJson(all)))
  }

  def create: Action[TenantBinder] =
    action(createTenant).async(parse.json[TenantBinder]) { req =>
      if (Tenant.isValidNewTenantName(req.body.name)) {
        tenantsService
          .create(Tenant(req.body.name))
          .map(_ => created(req.body.name))
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
