package be.thomastoye.speelsysteem.dashboard.controllers.api

import play.api.libs.json._
import play.api.mvc.{ Controller, Result }

class ApiController extends Controller {
  def created(id: String): Result = Created(Json.obj("status" -> "created", "id" -> id))
  def updated(id: String): Result = Ok(Json.obj("status" -> "updated", "id" -> id))
}
