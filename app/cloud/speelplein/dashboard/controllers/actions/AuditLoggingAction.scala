package cloud.speelplein.dashboard.controllers.actions

import java.time.Instant

import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.data.{AuditLogService, JwtVerificationService}
import javax.inject.Inject
import cloud.speelplein.models._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}

class AuditLoggingRequest[A](val tenantData: TenantUserData,
                             val tenant: Tenant,
                             val isGlobalSuperUser: Boolean,
                             val rawJwt: String,
                             request: JwtRequest[A])
    extends WrappedRequest[A](request)

class AuditLoggingAction @Inject()(
    eventId: String,
    data: AuditLogData,
    val parser: BodyParsers.Default,
    val auditLogService: AuditLogService,
    val jwtVerificationService: JwtVerificationService)(
    implicit val executionContext: ExecutionContext)
    extends ActionRefiner[JwtRequest, AuditLoggingRequest] {
  def refine[A](
      input: JwtRequest[A]): Future[Either[Result, AuditLoggingRequest[A]]] = {
    val userId = jwtVerificationService
      .decode(input.rawJwt)
      .map(Json.parse)
      .map(_ \ "sub")
      .map(_.as[String])
      .toOption

    val name = jwtVerificationService
      .decode(input.rawJwt)
      .map(Json.parse)
      .map(_ \ "name")
      .map(_.as[String])
      .toOption

    val triggeredBy =
      AuditLogTriggeredBy(name, userId, input.tenant.name, input.rawJwt)

    auditLogService
      .registerAuditLogEntry(
        AuditLogEntry(Instant.now,
                      Instant.now,
                      triggeredBy,
                      eventId,
                      data,
                      "backend"))(input.tenant)
      .map(
        _ =>
          new AuditLoggingRequest(input.tenantData,
                                  input.tenant,
                                  input.isGlobalSuperUser,
                                  input.rawJwt,
                                  input))
      .map(res => Right[Result, AuditLoggingRequest[A]](res))
  }
}

trait LoggingVerifyingBuilder {
  def logAndVerify(
      permission: Permission,
      data: AuditLogData): ActionFunction[TenantRequest, AuditLoggingRequest]
}

class LoggingVerifyingBuilderImpl @Inject()(
    jwtVerificationService: JwtVerificationService,
    parser: BodyParsers.Default,
    jwtVerifyAction: JwtVerifyAction,
    jwtBuilder: JwtAuthorizationBuilder,
    auditLogService: AuditLogService
)(implicit val ec: ExecutionContext)
    extends LoggingVerifyingBuilder {

  def logAndVerify(permission: Permission, data: AuditLogData)
    : ActionFunction[TenantRequest, AuditLoggingRequest] = {
    jwtBuilder.authenticate(permission) andThen new AuditLoggingAction(
      permission.id,
      data,
      parser,
      auditLogService,
      jwtVerificationService)
  }
}
