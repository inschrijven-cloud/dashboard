package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.dashboard.controllers.actions.{ DomainAction, JwtAuthorizationBuilder }
import be.thomastoye.speelsysteem.data.ContactPersonRepository
import be.thomastoye.speelsysteem.models.{ ContactPerson, JsonFormats }
import be.thomastoye.speelsysteem.models.JsonFormats.{ contactPersonFormat, contactPersonWithIdWrites, entityWithIdReads }
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent }

import scala.concurrent.ExecutionContext

class ContactPersonApiController @Inject() (
    contactPersonRepository: ContactPersonRepository,
    domainAction: DomainAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder
)(implicit ec: ExecutionContext) extends ApiController {

  def all: Action[AnyContent] = (Action andThen domainAction andThen jwtAuthorizationBuilder.authenticatePermission("contactperson:retrieve")).async { req =>
    contactPersonRepository.findAll(req.tenant).map(all => Ok(Json.toJson(all)))
  }

  def create: Action[EntityWithId[ContactPerson.Id, ContactPerson]] = (Action andThen domainAction andThen jwtAuthorizationBuilder.authenticatePermission("contactperson:create")).async(parse.json(entityWithIdReads[ContactPerson.Id, ContactPerson])) { req =>
    contactPersonRepository.insert(req.body.id, req.body.entity)(req.tenant).map(created)
  }

  def getById(id: ContactPerson.Id): Action[AnyContent] = (Action andThen domainAction andThen jwtAuthorizationBuilder.authenticatePermission("contactperson:retrieve")).async { req =>
    contactPersonRepository.findById(id)(req.tenant).map { personOpt =>
      personOpt.map(person => Json.toJson(person.entity)).map(Ok(_)).getOrElse(NotFound)
    }
  }

  def update(id: ContactPerson.Id): Action[ContactPerson] = (Action andThen domainAction andThen jwtAuthorizationBuilder.authenticatePermission("contactperson:update")).async(parse.json(JsonFormats.contactPersonFormat)) { req =>
    contactPersonRepository.update(id, req.body)(req.tenant).map(_ => updated(id))
  }

  def delete(id: ContactPerson.Id): Action[AnyContent] = (Action andThen domainAction andThen jwtAuthorizationBuilder.authenticatePermission("contactperson:delete")).async { req =>
    contactPersonRepository.delete(id)(req.tenant).map(_ => Ok)
  }
}
