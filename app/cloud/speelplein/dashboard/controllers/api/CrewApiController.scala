package cloud.speelplein.dashboard.controllers.api

import javax.inject.Inject

import cloud.speelplein.EntityWithId
import cloud.speelplein.dashboard.controllers.actions.{
  DomainAction,
  JwtAuthorizationBuilder
}
import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.dashboard.controllers.api.auth.Permission._
import cloud.speelplein.data.CrewRepository
import cloud.speelplein.models.Crew
import cloud.speelplein.models.JsonFormats.{
  crewFormat,
  crewWithIdWrites,
  entityWithIdReads
}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.ExecutionContext

class CrewApiController @Inject()(
    crewRepository: CrewRepository,
    domainAction: DomainAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder
)(implicit ec: ExecutionContext)
    extends ApiController {
  private def action(per: Permission) =
    Action andThen domainAction andThen jwtAuthorizationBuilder.authenticate(
      per)

  def create: Action[EntityWithId[Crew.Id, Crew]] =
    action(crewCreate).async(parse.json(entityWithIdReads[Crew.Id, Crew])) {
      req =>
        crewRepository
          .insert(req.body.id, req.body.entity)(req.tenant)
          .map(created)
    }

  def all: Action[AnyContent] = action(crewRetrieve).async { req =>
    crewRepository.findAll(req.tenant).map(all => Ok(Json.toJson(all)))
  }

  def getById(id: Crew.Id): Action[AnyContent] = action(crewRetrieve).async {
    req =>
      crewRepository.findById(id)(req.tenant).map { crewOpt =>
        crewOpt
          .map(crew => Json.toJson(crew.entity))
          .map(Ok(_))
          .getOrElse(NotFound)
      }
  }

  def update(id: Crew.Id): Action[Crew] =
    action(crewUpdate).async(parse.json(crewFormat)) { crewReq =>
      crewRepository
        .update(id, crewReq.body)(crewReq.tenant)
        .map(_ => updated(id))
    }

  def delete(id: Crew.Id): Action[AnyContent] = action(crewDelete).async {
    req =>
      crewRepository.delete(id)(req.tenant).map(_ => Ok)
  }
}
