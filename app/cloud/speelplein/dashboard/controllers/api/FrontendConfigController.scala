package cloud.speelplein.dashboard.controllers.api

import javax.inject.Inject

import cloud.speelplein.dashboard.controllers.api.auth.Permission.{
  createConfig,
  initializeAllConfigDb,
  listAllConfig
}
import cloud.speelplein.dashboard.controllers.actions.{
  TenantAction,
  GlobalTenantOnlyAction,
  JwtAuthorizationBuilder
}
import cloud.speelplein.data.ConfigService
import cloud.speelplein.models.{ConfigWrapper, Tenant}
import cloud.speelplein.models.JsonFormats.{configFormat, configWithIdWrites}
import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, BodyParsers}

import scala.concurrent.{ExecutionContext, Future}

class FrontendConfigController @Inject()(
    configService: ConfigService,
    tenantAction: TenantAction,
    globalTenantOnlyAction: GlobalTenantOnlyAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder
)(implicit ec: ExecutionContext)
    extends ApiController {

  def configJson: Action[AnyContent] = (Action andThen tenantAction).async {
    req =>
      configService.getConfig(req.tenant.name).map { configOpt =>
        configOpt.map(conf => Ok(conf.config)) getOrElse NotFound(
          Json.obj("status" -> "not found", "tenant" -> req.tenant.name))
      }
  }

  def getAllConfig: Action[AnyContent] =
    (Action andThen tenantAction andThen globalTenantOnlyAction andThen jwtAuthorizationBuilder
      .authenticate(listAllConfig)).async { req =>
      configService.getAllConfig.map(res => Ok(Json.toJson(res)))
    }

  def generateDesignDocs(): Action[AnyContent] =
    (Action andThen tenantAction andThen globalTenantOnlyAction andThen jwtAuthorizationBuilder
      .authenticate(initializeAllConfigDb)).async { req =>
      configService.insertDesignDocs.map(Ok(_))
    }

  def insertConfigDocument(tenant: String): Action[ConfigWrapper] =
    (Action andThen tenantAction andThen globalTenantOnlyAction andThen jwtAuthorizationBuilder
      .authenticate(createConfig)).async(parse.json(configFormat)) { req =>
      configService
        .insert(tenant, ConfigWrapper(req.body.config))
        .map(_ => created(tenant))
    }

  def updateConfigDocument(tenant: String): Action[ConfigWrapper] =
    (Action andThen tenantAction andThen globalTenantOnlyAction andThen jwtAuthorizationBuilder
      .authenticate(createConfig)).async(parse.json(configFormat)) { req =>
      configService
        .update(tenant, ConfigWrapper(req.body.config))
        .map(_ => updated(tenant))
    }
}
