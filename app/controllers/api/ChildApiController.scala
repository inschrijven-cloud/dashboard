package controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.data.ChildRepository
import be.thomastoye.speelsysteem.data.util.UuidService
import be.thomastoye.speelsysteem.models.JsonFormats
import play.api.mvc._
import be.thomastoye.speelsysteem.models.JsonFormats.childWithIdWrites
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._

class ChildApiController @Inject() (childRepository: ChildRepository, uuidService: UuidService) extends ApiController {

  def all = Action.async { req => childRepository.findAll.map(all => Ok(Json.toJson(all))) }

  def create = Action.async(BodyParsers.parse.json(JsonFormats.childFormat)) { req =>
    childRepository.insert(uuidService.random, req.body).map(created)
  }
}
