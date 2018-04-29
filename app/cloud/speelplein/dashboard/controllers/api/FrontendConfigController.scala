package cloud.speelplein.dashboard.controllers.api

import javax.inject.Inject
import cloud.speelplein.dashboard.controllers.api.auth.Permission.{
  createConfig,
  initializeAllConfigDb,
  listAllConfig
}
import cloud.speelplein.dashboard.controllers.actions._
import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.data.ConfigService
import cloud.speelplein.models.{AuditLogData, ConfigWrapper}
import cloud.speelplein.models.JsonFormats.{configFormat, configWithIdWrites}
import play.api.libs.json.Json
import play.api.mvc.{Action, ActionBuilder, AnyContent}

import scala.concurrent.{ExecutionContext, Future}

class FrontendConfigController @Inject()(
    configService: ConfigService,
    tenantAction: TenantAction,
    globalTenantOnlyAction: GlobalTenantOnlyAction,
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

  def configJson: Action[AnyContent] = (Action andThen tenantAction).async {
    req =>
      configService.getConfig(req.tenant.name).map { configOpt =>
        configOpt.map(conf => Ok(conf.config)) getOrElse NotFound(
          Json.obj("status" -> "not found", "tenant" -> req.tenant.name))
      }
  }

  def getAllConfig: Action[AnyContent] =
    action(listAllConfig).async { req =>
      configService.getAllConfig.map(res => Ok(Json.toJson(res)))
    }

  def generateDesignDocs(): Action[AnyContent] =
    action(initializeAllConfigDb).async { req =>
      configService.insertDesignDocs.map(Ok(_))
    }

  def insertConfigDocument(tenant: String): Action[ConfigWrapper] =
    action(createConfig, AuditLogData.tenantName(tenant))
      .async(parse.json(configFormat)) { req =>
        configService
          .insert(tenant, ConfigWrapper(req.body.config))
          .map(_ => created(tenant))
      }

  def updateConfigDocument(tenant: String): Action[ConfigWrapper] =
    action(createConfig, AuditLogData.tenantName(tenant))
      .async(parse.json(configFormat)) { req =>
        configService
          .update(tenant, ConfigWrapper(req.body.config))
          .map(_ => updated(tenant))
      }
}
