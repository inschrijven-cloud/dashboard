package cloud.speelplein.dashboard.controllers.api

import javax.inject.Inject

import cloud.speelplein.EntityWithId
import cloud.speelplein.dashboard.controllers.actions.{ DomainAction, JwtAuthorizationBuilder }
import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.dashboard.controllers.api.auth.Permission._
import cloud.speelplein.models.JsonFormats
import cloud.speelplein.models.JsonFormats.{ contactPersonFormat, contactPersonWithIdWrites, entityWithIdReads }
import cloud.speelplein.dashboard.controllers.actions.{ DomainAction, JwtAuthorizationBuilder }
import cloud.speelplein.data.ContactPersonRepository
import cloud.speelplein.models.{ ContactPerson, JsonFormats }
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent }

import scala.concurrent.ExecutionContext

class ContactPersonApiController @Inject() (
    contactPersonRepository: ContactPersonRepository,
    domainAction: DomainAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder
)(implicit ec: ExecutionContext) extends ApiController {
  private def action(per: Permission) = Action andThen domainAction andThen jwtAuthorizationBuilder.authenticate(per)

  def all: Action[AnyContent] = action(contactPersonRetrieve).async { req =>
    contactPersonRepository.findAll(req.tenant).map(all => Ok(Json.toJson(all)))
  }

  def create: Action[EntityWithId[ContactPerson.Id, ContactPerson]] = action(contactPersonCreate).async(parse.json(entityWithIdReads[ContactPerson.Id, ContactPerson])) { req =>
    contactPersonRepository.insert(req.body.id, req.body.entity)(req.tenant).map(created)
  }

  def getById(id: ContactPerson.Id): Action[AnyContent] = action(contactPersonRetrieve).async { req =>
    contactPersonRepository.findById(id)(req.tenant).map { personOpt =>
      personOpt.map(person => Json.toJson(person.entity)).map(Ok(_)).getOrElse(NotFound)
    }
  }

  def update(id: ContactPerson.Id): Action[ContactPerson] = action(contactPersonUpdate).async(parse.json(JsonFormats.contactPersonFormat)) { req =>
    contactPersonRepository.update(id, req.body)(req.tenant).map(_ => updated(id))
  }

  def delete(id: ContactPerson.Id): Action[AnyContent] = action(contactPersonDelete).async { req =>
    contactPersonRepository.delete(id)(req.tenant).map(_ => Ok)
  }
}
