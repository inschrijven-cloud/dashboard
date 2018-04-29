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
import cloud.speelplein.data.ChildRepository
import cloud.speelplein.models.{AuditLogData, Child}
import cloud.speelplein.models.JsonFormats.{
  childFormat,
  childWithIdWrites,
  entityWithIdReads
}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class ChildApiController @Inject()(
    childRepository: ChildRepository,
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

  def all: Action[AnyContent] = action(childRetrieve).async { req =>
    childRepository.findAll(req.tenant).map(all => Ok(Json.toJson(all)))
  }

  def create: Action[EntityWithId[Child.Id, Child]] =
    action(childCreate).async(parse.json(entityWithIdReads[Child.Id, Child])) {
      req =>
        childRepository
          .insert(req.body.id, req.body.entity)(req.tenant)
          .map(created)
    }

  def getById(id: Child.Id): Action[AnyContent] =
    action(childRetrieve, AuditLogData.childId(id)).async { req =>
      childRepository.findById(id)(req.tenant).map { childOpt =>
        childOpt
          .map(child => Json.toJson(child.entity))
          .map(Ok(_))
          .getOrElse(NotFound)
      }
    }

  def update(id: Child.Id): Action[Child] =
    action(childUpdate, AuditLogData.childId(id))
      .async(parse.json(childFormat)) { req =>
        childRepository.update(id, req.body)(req.tenant).map(_ => updated(id))
      }

  def delete(id: Child.Id): Action[AnyContent] =
    action(childDelete, AuditLogData.childId(id)).async { req =>
      childRepository.delete(id)(req.tenant).map(_ => Ok)
    }

  def merge(retiredId: Child.Id, absorbedIntoId: Child.Id): Action[AnyContent] =
    action(childMerge).async { req =>
      childRepository
        .setMerged(retiredId, absorbedIntoId)(req.tenant)
        .map(_ => Ok)
    }
}
