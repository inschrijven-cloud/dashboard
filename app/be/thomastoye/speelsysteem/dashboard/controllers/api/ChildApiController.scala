package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.dashboard.controllers.actions.DomainAction
import be.thomastoye.speelsysteem.data.ChildRepository
import be.thomastoye.speelsysteem.models.Child
import be.thomastoye.speelsysteem.models.JsonFormats.{ childFormat, childWithIdWrites, entityWithIdReads }
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class ChildApiController @Inject() (childRepository: ChildRepository, domainAction: DomainAction)(implicit ec: ExecutionContext) extends ApiController {

  def all: Action[AnyContent] = (Action andThen domainAction).async { req =>
    childRepository.findAll(req.tenant).map(all => Ok(Json.toJson(all)))
  }

  def create: Action[EntityWithId[Child.Id, Child]] = (Action andThen domainAction).async(parse.json(entityWithIdReads[Child.Id, Child])) { req =>
    childRepository.insert(req.body.id, req.body.entity)(req.tenant).map(created)
  }

  def getById(id: Child.Id): Action[AnyContent] = (Action andThen domainAction).async { req =>
    childRepository.findById(id)(req.tenant).map { childOpt =>
      childOpt.map(child => Json.toJson(child.entity)).map(Ok(_)).getOrElse(NotFound)
    }
  }

  def update(id: Child.Id): Action[Child] = (Action andThen domainAction).async(parse.json(childFormat)) { req =>
    childRepository.update(id, req.body)(req.tenant).map(_ => updated(id))
  }

  def delete(id: Child.Id): Action[AnyContent] = (Action andThen domainAction).async { req =>
    childRepository.delete(id)(req.tenant).map(_ => Ok)
  }

  def merge(retiredId: Child.Id, absorpedIntoId: Child.Id): Action[AnyContent] = (Action andThen domainAction).async { req =>
    childRepository.setMerged(retiredId, absorpedIntoId)(req.tenant).map(_ => Ok)
  }
}
