package be.thomastoye.speelsysteem.dashboard.controllers

import play.api.libs.json.Json
import play.api.mvc._

class ApplicationController extends Controller {
  def heartbeat: Action[AnyContent] = Action {
    Ok(Json.obj("status" -> "ok", "statusCode" -> "200", "message" -> "online"))
  }
}
