package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.data.ChildRepository
import be.thomastoye.speelsysteem.data.util.UuidService
import be.thomastoye.speelsysteem.models.{ Child, JsonFormats }
import be.thomastoye.speelsysteem.models.JsonFormats.{ childWithIdWrites, childFormat }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.mvc._

class ChildApiController @Inject() (childRepository: ChildRepository, uuidService: UuidService) extends ApiController {

  def all: Action[AnyContent] = Action.async { req =>
    childRepository.findAll.map(all => Ok(Json.toJson(all)))
  }

  def create: Action[Child] = Action.async(BodyParsers.parse.json(JsonFormats.childFormat)) { req =>
    childRepository.insert(uuidService.random, req.body).map(created)
  }

  def getById(id: Child.Id): Action[AnyContent] = Action.async { req =>
    childRepository.findById(id).map { childOpt =>
      childOpt.map(child => Json.toJson(child.entity)).map(Ok(_)).getOrElse(NotFound)
    }
  }

  def update(id: Child.Id): Action[Child] = Action.async(parse.json(childFormat)) { childReq =>
    childRepository.update(id, childReq.body).map(_ => Ok)
  }

  def delete(id: Child.Id): Action[AnyContent] = Action.async { req =>
    childRepository.delete(id).map(_ => Ok)
  }
}
