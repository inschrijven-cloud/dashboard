package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.data.ChildRepository
import be.thomastoye.speelsysteem.models.{ Child, JsonFormats }
import be.thomastoye.speelsysteem.models.JsonFormats.{ childFormat, childWithIdWrites, entityWithIdReads }
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class ChildApiController @Inject() (childRepository: ChildRepository)(implicit ec: ExecutionContext) extends ApiController {

  def all: Action[AnyContent] = Action.async { req =>
    childRepository.findAll.map(all => Ok(Json.toJson(all)))
  }

  def create: Action[EntityWithId[Child.Id, Child]] = Action.async(parse.json(entityWithIdReads[Child.Id, Child])) { req =>
    childRepository.insert(req.body.id, req.body.entity).map(created)
  }

  def getById(id: Child.Id): Action[AnyContent] = Action.async { req =>
    childRepository.findById(id).map { childOpt =>
      childOpt.map(child => Json.toJson(child.entity)).map(Ok(_)).getOrElse(NotFound)
    }
  }

  def update(id: Child.Id): Action[Child] = Action.async(parse.json(childFormat)) { childReq =>
    childRepository.update(id, childReq.body).map(_ => updated(id))
  }

  def delete(id: Child.Id): Action[AnyContent] = Action.async { req =>
    childRepository.delete(id).map(_ => Ok)
  }
}
