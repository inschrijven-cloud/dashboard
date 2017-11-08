package cloud.speelplein.dashboard.controllers.api

import play.api.libs.json._
import play.api.mvc.{ InjectedController, Result }

class ApiController extends InjectedController {
  def created(id: String): Result = Created(Json.obj("status" -> "created", "id" -> id))
  def updated(id: String): Result = Ok(Json.obj("status" -> "updated", "id" -> id))
}
