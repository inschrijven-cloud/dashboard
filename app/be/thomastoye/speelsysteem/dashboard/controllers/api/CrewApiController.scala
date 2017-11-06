package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.dashboard.controllers.actions.{ DomainAction, JwtAuthorizationBuilder }
import be.thomastoye.speelsysteem.dashboard.controllers.api.auth.Permission._
import be.thomastoye.speelsysteem.data.CrewRepository
import be.thomastoye.speelsysteem.models.Crew
import be.thomastoye.speelsysteem.models.JsonFormats.{ crewFormat, crewWithIdWrites, entityWithIdReads }
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent }

import scala.concurrent.ExecutionContext

class CrewApiController @Inject() (
    crewRepository: CrewRepository,
    domainAction: DomainAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder
)(implicit ec: ExecutionContext) extends ApiController {

  def create: Action[EntityWithId[Crew.Id, Crew]] = (Action andThen domainAction andThen jwtAuthorizationBuilder.authenticate(crewCreate)).async(parse.json(entityWithIdReads[Crew.Id, Crew])) { req =>
    crewRepository.insert(req.body.id, req.body.entity)(req.tenant).map(created)
  }

  def all: Action[AnyContent] = (Action andThen domainAction andThen jwtAuthorizationBuilder.authenticate(crewRetrieve)).async { req =>
    crewRepository.findAll(req.tenant).map(all => Ok(Json.toJson(all)))
  }

  def getById(id: Crew.Id): Action[AnyContent] = (Action andThen domainAction andThen jwtAuthorizationBuilder.authenticate(crewRetrieve)).async { req =>
    crewRepository.findById(id)(req.tenant).map { crewOpt =>
      crewOpt.map(crew => Json.toJson(crew.entity)).map(Ok(_)).getOrElse(NotFound)
    }
  }

  def update(id: Crew.Id): Action[Crew] = (Action andThen domainAction andThen jwtAuthorizationBuilder.authenticate(crewUpdate)).async(parse.json(crewFormat)) { crewReq =>
    crewRepository.update(id, crewReq.body)(crewReq.tenant).map(_ => updated(id))
  }

  def delete(id: Crew.Id): Action[AnyContent] = (Action andThen domainAction andThen jwtAuthorizationBuilder.authenticate(crewDelete)).async { req =>
    crewRepository.delete(id)(req.tenant).map(_ => Ok)
  }
}
