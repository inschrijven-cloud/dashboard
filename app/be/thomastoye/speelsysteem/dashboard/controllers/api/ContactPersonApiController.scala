package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.data.ContactPersonRepository
import be.thomastoye.speelsysteem.models.{ ContactPerson, JsonFormats }
import be.thomastoye.speelsysteem.models.JsonFormats.{ contactPersonFormat, contactPersonWithIdWrites, entityWithIdReads }
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent }

import scala.concurrent.ExecutionContext

class ContactPersonApiController @Inject() (
    contactPersonRepository: ContactPersonRepository
)(implicit ec: ExecutionContext) extends ApiController {

  def all: Action[AnyContent] = Action.async { req =>
    contactPersonRepository.findAll.map(all => Ok(Json.toJson(all)))
  }

  def create: Action[EntityWithId[ContactPerson.Id, ContactPerson]] = Action.async(parse.json(entityWithIdReads[ContactPerson.Id, ContactPerson])) { req =>
    contactPersonRepository.insert(req.body.id, req.body.entity).map(created)
  }

  def getById(id: ContactPerson.Id): Action[AnyContent] = Action.async { req =>
    contactPersonRepository.findById(id).map { personOpt =>
      personOpt.map(person => Json.toJson(person.entity)).map(Ok(_)).getOrElse(NotFound)
    }
  }

  def update(id: ContactPerson.Id): Action[ContactPerson] = Action.async(parse.json(JsonFormats.contactPersonFormat)) { req =>
    contactPersonRepository.update(id, req.body).map(_ => updated(id))
  }

  def delete(id: ContactPerson.Id): Action[AnyContent] = Action.async { req =>
    contactPersonRepository.delete(id).map(_ => Ok)
  }
}
