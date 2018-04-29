package cloud.speelplein.dashboard.controllers.api

import javax.inject.Inject
import cloud.speelplein.dashboard.controllers.actions._
import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.dashboard.controllers.api.auth.Permission._
import cloud.speelplein.data.TenantsService
import cloud.speelplein.data.couchdb.RemoteDbConfigImpl
import cloud.speelplein.models.JsonFormats.tenantFormat
import cloud.speelplein.models.{AuditLogData, Tenant}
import play.api.libs.json.Json
import play.api.mvc.{Action, ActionBuilder, AnyContent}

import scala.concurrent.{ExecutionContext, Future}

class TenantsController @Inject()(
    tenantAction: TenantAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder,
    globalTenantOnlyAction: GlobalTenantOnlyAction,
    tenantsService: TenantsService,
    remoteCouchDB: RemoteDbConfigImpl,
    auditAuthorizationBuilder: LoggingVerifyingBuilder
)(implicit ec: ExecutionContext)
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

  def list: Action[AnyContent] = action(listTenants).async { req =>
    tenantsService.all.map(all => Ok(Json.toJson(all)))
  }

  def create(name: String): Action[AnyContent] =
    action(createTenant, AuditLogData.tenantName(name)).async { req =>
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
    action(initTenantDbs, AuditLogData.tenantName(tenant)).async { req =>
      if (Tenant.isValidNewTenantName(tenant)) {
        tenantsService
          .initializeDatabase(Tenant.apply(tenant))
          .map(res => Ok(res))
      } else {
        Future.successful(BadRequest("Invalid tenant name"))
      }
    }

  def syncTo(tenant: String): Action[AnyContent] =
    action(syncTenantDb, AuditLogData.tenantName(tenant)).async { req =>
      tenantsService.syncTo(Tenant(tenant), remoteCouchDB).map(res => Ok(res))
    }

  def syncFrom(tenant: String): Action[AnyContent] =
    action(syncTenantDb, AuditLogData.tenantName(tenant)).async { req =>
      tenantsService.syncFrom(Tenant(tenant), remoteCouchDB).map(res => Ok(res))
    }
}
