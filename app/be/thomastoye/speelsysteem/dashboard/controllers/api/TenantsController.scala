package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.dashboard.controllers.actions.{ DomainAction, GlobalDomainOnlyAction, JwtAuthorizationBuilder }
import be.thomastoye.speelsysteem.dashboard.controllers.api.TenantsController.TenantBinder
import be.thomastoye.speelsysteem.dashboard.controllers.api.auth.Permission._
import be.thomastoye.speelsysteem.data.TenantsService
import be.thomastoye.speelsysteem.data.couchdb.RemoteDbConfigImp
import be.thomastoye.speelsysteem.models.JsonFormats.tenantFormat
import be.thomastoye.speelsysteem.models.Tenant
import play.api.libs.json.Json

import scala.concurrent.{ ExecutionContext, Future }

object TenantsController {
  case class TenantBinder(name: String)
  implicit val tenantBinderFormat = Json.format[TenantBinder]
}

class TenantsController @Inject() (
    domainAction: DomainAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder,
    globalDomainOnlyAction: GlobalDomainOnlyAction,
    tenantsService: TenantsService,
    remoteCouchDB: RemoteDbConfigImp
)(implicit ec: ExecutionContext) extends ApiController {

  def list = (Action andThen domainAction andThen globalDomainOnlyAction andThen jwtAuthorizationBuilder.authenticate(listTenants)).async { req =>
    tenantsService.all.map(all => Ok(Json.toJson(all)))
  }

  def create = (Action andThen domainAction andThen globalDomainOnlyAction andThen jwtAuthorizationBuilder.authenticate(createTenant))
    .async(parse.json[TenantBinder]) { req =>
      if (Tenant.isValidNewTenantName(req.body.name)) {
        tenantsService.create(Tenant(req.body.name)).map(_ => created(req.body.name))
      } else Future.successful(BadRequest(Json.obj("status" -> "failed", "reason" -> "Tenant name contains invalid characters")))
    }

  def details(tenant: String) = TODO

  def generateDesignDocs(tenant: String) = (Action andThen domainAction andThen globalDomainOnlyAction andThen jwtAuthorizationBuilder.authenticate(initTenantDbs)).async { req =>
    tenantsService.initializeDatabase(req.tenant).map(res => Ok(res))
  }

  def syncTo(tenant: String) = (Action andThen domainAction andThen globalDomainOnlyAction andThen jwtAuthorizationBuilder.authenticate(syncTenantDb)).async { req =>
    tenantsService.syncTo(Tenant(tenant), remoteCouchDB).map(res => Ok(res))
  }

  def syncFrom(tenant: String) = (Action andThen domainAction andThen globalDomainOnlyAction andThen jwtAuthorizationBuilder.authenticate(syncTenantDb)).async { req =>
    tenantsService.syncFrom(Tenant(tenant), remoteCouchDB).map(res => Ok(res))
  }
}
