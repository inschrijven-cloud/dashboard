package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.dashboard.controllers.actions.DomainAction
import be.thomastoye.speelsysteem.data.ConfigService
import be.thomastoye.speelsysteem.models.JsonFormats.configFormat
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
