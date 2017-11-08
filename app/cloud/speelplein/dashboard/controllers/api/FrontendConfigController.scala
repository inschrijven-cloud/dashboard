package cloud.speelplein.dashboard.controllers.api

import javax.inject.Inject

import cloud.speelplein.dashboard.controllers.actions.DomainAction
import cloud.speelplein.models.JsonFormats.configFormat
import cloud.speelplein.dashboard.controllers.actions.DomainAction
import cloud.speelplein.data.ConfigService
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent }

import scala.concurrent.ExecutionContext

class FrontendConfigController @Inject() (
    configService: ConfigService,
    domainAction: DomainAction
)(implicit ec: ExecutionContext) extends ApiController {
  def configJson: Action[AnyContent] = (Action andThen domainAction).async { req =>
    configService.getConfig(req.userDomain).map { configOpt =>
      configOpt.map(conf => Ok(Json.toJson(conf))) getOrElse NotFound(Json.obj("status" -> "not found", "domain" -> req.userDomain))
    }
  }
}
