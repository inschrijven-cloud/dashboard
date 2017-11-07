package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.dashboard.controllers.actions.{ DomainAction, JwtAuthorizationBuilder }
import be.thomastoye.speelsysteem.dashboard.controllers.api.auth.Permission
import be.thomastoye.speelsysteem.dashboard.controllers.api.auth.Permission._
import be.thomastoye.speelsysteem.data.ChildRepository
import be.thomastoye.speelsysteem.models.Child
import be.thomastoye.speelsysteem.models.JsonFormats.{ childFormat, childWithIdWrites, entityWithIdReads }
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class ChildApiController @Inject() (
    childRepository: ChildRepository,
    domainAction: DomainAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder
)(implicit ec: ExecutionContext) extends ApiController {
  private def action(perm: Permission) = Action andThen domainAction andThen jwtAuthorizationBuilder.authenticate(perm)

  def all: Action[AnyContent] = action(childRetrieve).async { req =>
    childRepository.findAll(req.tenant).map(all => Ok(Json.toJson(all)))
  }

  def create: Action[EntityWithId[Child.Id, Child]] = action(childCreate).async(parse.json(entityWithIdReads[Child.Id, Child])) { req =>
    childRepository.insert(req.body.id, req.body.entity)(req.tenant).map(created)
  }

  def getById(id: Child.Id): Action[AnyContent] = action(childRetrieve).async { req =>
    childRepository.findById(id)(req.tenant).map { childOpt =>
      childOpt.map(child => Json.toJson(child.entity)).map(Ok(_)).getOrElse(NotFound)
    }
  }

  def update(id: Child.Id): Action[Child] = action(childUpdate).async(parse.json(childFormat)) { req =>
    childRepository.update(id, req.body)(req.tenant).map(_ => updated(id))
  }

  def delete(id: Child.Id): Action[AnyContent] = action(childDelete).async { req =>
    childRepository.delete(id)(req.tenant).map(_ => Ok)
  }

  def merge(retiredId: Child.Id, absorpedIntoId: Child.Id): Action[AnyContent] = action(childMerge).async { req =>
    childRepository.setMerged(retiredId, absorpedIntoId)(req.tenant).map(_ => Ok)
  }
}
