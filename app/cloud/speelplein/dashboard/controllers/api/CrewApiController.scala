package cloud.speelplein.dashboard.controllers.api

import javax.inject.Inject
import cloud.speelplein.EntityWithId
import cloud.speelplein.dashboard.controllers.actions.{
  AuditLoggingRequest,
  JwtAuthorizationBuilder,
  LoggingVerifyingBuilder,
  TenantAction
}
import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.dashboard.controllers.api.auth.Permission._
import cloud.speelplein.data.CrewRepository
import cloud.speelplein.models.{AuditLogData, Crew}
import cloud.speelplein.models.JsonFormats.{
  crewFormat,
  crewWithIdWrites,
  entityWithIdReads
}
import play.api.libs.json.Json
import play.api.mvc.{Action, ActionBuilder, AnyContent}

import scala.concurrent.ExecutionContext

class CrewApiController @Inject()(
    crewRepository: CrewRepository,
    tenantAction: TenantAction,
    auditAuthorizationBuilder: LoggingVerifyingBuilder
)(implicit ec: ExecutionContext)
    extends ApiController {

  private def action(
      perm: Permission,
      data: AuditLogData): ActionBuilder[AuditLoggingRequest, AnyContent] =
    Action andThen tenantAction andThen auditAuthorizationBuilder.logAndVerify(
      perm,
      data)

  private def action(
      perm: Permission): ActionBuilder[AuditLoggingRequest, AnyContent] =
    action(perm, AuditLogData.empty)

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

  def getById(id: Crew.Id): Action[AnyContent] =
    action(crewRetrieve, AuditLogData.crewId(id)).async { req =>
      crewRepository.findById(id)(req.tenant).map { crewOpt =>
        crewOpt
          .map(crew => Json.toJson(crew.entity))
          .map(Ok(_))
          .getOrElse(NotFound)
      }
    }

  def update(id: Crew.Id): Action[Crew] =
    action(crewUpdate, AuditLogData.crewId(id)).async(parse.json(crewFormat)) {
      crewReq =>
        crewRepository
          .update(id, crewReq.body)(crewReq.tenant)
          .map(_ => updated(id))
    }

  def delete(id: Crew.Id): Action[AnyContent] =
    action(crewDelete, AuditLogData.crewId(id)).async { req =>
      crewRepository.delete(id)(req.tenant).map(_ => Ok)
    }
}
