package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.data.ChildRepository
import be.thomastoye.speelsysteem.data.util.UuidService
import be.thomastoye.speelsysteem.models.{Child, JsonFormats}
import be.thomastoye.speelsysteem.models.JsonFormats.childWithIdWrites
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.mvc._

class ChildApiController @Inject() (childRepository: ChildRepository, uuidService: UuidService) extends ApiController {

  def all = Action.async { req => childRepository.findAll.map(all => Ok(Json.toJson(all))) }

  def create = Action.async(BodyParsers.parse.json(JsonFormats.childFormat)) { req =>
    childRepository.insert(uuidService.random, req.body).map(created)
  }

  def getById(id: Child.Id) = TODO

  def update = TODO

  def delete(id: Child.Id) = TODO
}
