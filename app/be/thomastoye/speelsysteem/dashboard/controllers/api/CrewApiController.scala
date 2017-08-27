package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.data.CrewRepository
import be.thomastoye.speelsysteem.models.Crew
import be.thomastoye.speelsysteem.models.JsonFormats.{ crewFormat, crewWithIdWrites, entityWithIdReads }
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent }

import scala.concurrent.ExecutionContext

class CrewApiController @Inject() (crewRepository: CrewRepository)(implicit ec: ExecutionContext) extends ApiController {
  def create: Action[EntityWithId[Crew.Id, Crew]] = Action.async(parse.json(entityWithIdReads[Crew.Id, Crew])) { req =>
    crewRepository.insert(req.body.id, req.body.entity).map(created)
  }

  def all: Action[AnyContent] = Action.async { req =>
    crewRepository.findAll.map(all => Ok(Json.toJson(all)))
  }

  def getById(id: Crew.Id): Action[AnyContent] = Action.async { req =>
    crewRepository.findById(id).map { crewOpt =>
      crewOpt.map(crew => Json.toJson(crew.entity)).map(Ok(_)).getOrElse(NotFound)
    }
  }

  def update(id: Crew.Id): Action[Crew] = Action.async(parse.json(crewFormat)) { crewReq =>
    crewRepository.update(id, crewReq.body).map(_ => updated(id))
  }

  def delete(id: Crew.Id): Action[AnyContent] = Action.async { req =>
    crewRepository.delete(id).map(_ => Ok)
  }
}
