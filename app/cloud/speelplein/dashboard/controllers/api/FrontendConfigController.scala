package cloud.speelplein.dashboard.controllers.api

import javax.inject.Inject

import cloud.speelplein.dashboard.controllers.api.auth.Permission.{
  createConfig,
  initializeAllConfigDb,
  listAllConfig
}
import cloud.speelplein.dashboard.controllers.actions.{
  DomainAction,
  GlobalDomainOnlyAction,
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
    domainAction: DomainAction,
    globalDomainOnlyAction: GlobalDomainOnlyAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder
)(implicit ec: ExecutionContext)
    extends ApiController {

  def configJson: Action[AnyContent] = (Action andThen domainAction).async {
    req =>
      configService.getConfig(req.userDomain).map { configOpt =>
        configOpt.map(conf => Ok(conf.config)) getOrElse NotFound(
          Json.obj("status" -> "not found", "domain" -> req.userDomain))
      }
  }

  def getAllConfig: Action[AnyContent] =
    (Action andThen domainAction andThen globalDomainOnlyAction andThen jwtAuthorizationBuilder
      .authenticate(listAllConfig)).async { req =>
      configService.getAllConfig.map(res => Ok(Json.toJson(res)))
    }

  def generateDesignDocs(): Action[AnyContent] =
    (Action andThen domainAction andThen globalDomainOnlyAction andThen jwtAuthorizationBuilder
      .authenticate(initializeAllConfigDb)).async { req =>
      configService.insertDesignDocs.map(Ok(_))
    }

  def insertConfigDocument(domain: String): Action[ConfigWrapper] =
    (Action andThen domainAction andThen globalDomainOnlyAction andThen jwtAuthorizationBuilder
      .authenticate(createConfig)).async(parse.json(configFormat)) { req =>
      configService
        .insert(domain, ConfigWrapper(req.body.config))
        .map(_ => created(domain))
    }

  def updateConfigDocument(domain: String): Action[ConfigWrapper] =
    (Action andThen domainAction andThen globalDomainOnlyAction andThen jwtAuthorizationBuilder
      .authenticate(createConfig)).async(parse.json(configFormat)) { req =>
      configService
        .update(domain, ConfigWrapper(req.body.config))
        .map(_ => updated(domain))
    }
}
