package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.data.ContactPersonRepository
import be.thomastoye.speelsysteem.data.util.UuidService
import be.thomastoye.speelsysteem.models.{ ContactPerson, JsonFormats }
import be.thomastoye.speelsysteem.models.JsonFormats.{ contactPersonWithIdWrites, contactPersonFormat }
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{ Action, AnyContent, BodyParsers }

class ContactPersonApiController @Inject() (
    contactPersonRepository: ContactPersonRepository,
    uuidService: UuidService
) extends ApiController {

  def all: Action[AnyContent] = Action.async { req =>
    contactPersonRepository.findAll.map(all => Ok(Json.toJson(all)))
  }

  def create: Action[ContactPerson] = Action.async(BodyParsers.parse.json(JsonFormats.contactPersonFormat)) { req =>
    contactPersonRepository.insert(uuidService.random, req.body).map(created)
  }

  def getById(id: ContactPerson.Id): Action[AnyContent] = Action.async { req =>
    contactPersonRepository.findById(id).map { childOpt =>
      childOpt.map(child => Json.toJson(child.entity)).map(Ok(_)).getOrElse(NotFound)
    }
  }

  def update(id: ContactPerson.Id): Action[ContactPerson] = Action.async(parse.json(JsonFormats.contactPersonFormat)) { childReq =>
    contactPersonRepository.update(id, childReq.body).map(_ => updated(id))
  }

  def delete(id: ContactPerson.Id): Action[AnyContent] = Action.async { req =>
    contactPersonRepository.delete(id).map(_ => Ok)
  }
}
