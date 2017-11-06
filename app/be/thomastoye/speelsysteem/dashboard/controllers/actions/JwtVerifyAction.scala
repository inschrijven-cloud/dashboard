package be.thomastoye.speelsysteem.dashboard.controllers.actions

import javax.inject.Inject

import be.thomastoye.speelsysteem.models.JsonFormats.auth0AppMetadata
import be.thomastoye.speelsysteem.dashboard.services.JwtVerificationService
import be.thomastoye.speelsysteem.models.{ Auth0AppMetadata, TenantMetadata, Tenant }
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

class JwtRequest[A](val tenantData: TenantMetadata, val userDomain: String, val tenant: Tenant, request: Request[A]) extends WrappedRequest[A](request)

class JwtVerifyAction @Inject() (
  jwtVerificationService: JwtVerificationService,
  parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends ActionRefiner[DomainRequest, JwtRequest] {

  def refine[A](input: DomainRequest[A]): Future[Either[Result, JwtRequest[A]]] = Future.successful {
    val t = input.headers.get("Authorization").map(Success(_)).getOrElse(Failure(new Exception("Authorization header not found or Bearer not set")))
      .map(header => header.drop("Bearer ".length))
      .flatMap(token => jwtVerificationService.decode(token))
      .map(json => (Json.parse(json) \ "https://inschrijven.cloud/app_metadata").as[Auth0AppMetadata])
      .map(metadata => metadata.tenants.find(_.name == input.tenant.name))
      .flatMap(opt => opt.map(Success(_)).getOrElse(Failure(new Exception(s"Tenant '${input.tenant.name}' not found in JWT token"))))
      .map(obj => new JwtRequest[A](obj, input.userDomain, input.tenant, input))

    t match {
      case Success(x) => Right(x)
      case Failure(e) => Left(BadRequest(Json.obj("status" -> "error", "reason" -> "Missing or invalid JWT", "details" -> e.getMessage)))
    }
  }
}

trait JwtAuthorizationBuilder {
  def authenticate(permissions: Seq[String] = Seq.empty, roles: Seq[String] = Seq.empty): ActionFunction[DomainRequest, JwtRequest]

  def authenticatePermission(permission: String): ActionFunction[DomainRequest, JwtRequest] = authenticate(Seq(permission), Seq.empty)

  def authenticateRole(role: String): ActionFunction[DomainRequest, JwtRequest] = authenticate(Seq.empty, Seq(role))
}

class JwtAuthorizationBuilderImpl @Inject() (
    jwtVerificationService: JwtVerificationService,
    parser: BodyParsers.Default,
    jwtVerifyAction: JwtVerifyAction
)(implicit val ec: ExecutionContext) extends JwtAuthorizationBuilder {

  def authenticate(permissions: Seq[String] = Seq.empty, roles: Seq[String] = Seq.empty): ActionFunction[DomainRequest, JwtRequest] = {
    jwtVerifyAction andThen new ActionRefiner[JwtRequest, JwtRequest] {
      def executionContext: ExecutionContext = ec

      def refine[A](request: JwtRequest[A]): Future[Either[Result, JwtRequest[A]]] = {
        if (request.tenantData.permissions.intersect(permissions).nonEmpty || request.tenantData.roles.intersect(roles).nonEmpty) {
          Future.successful(Right(request))
        } else Future.successful(Left(Unauthorized(Json.obj(
          "status" -> "error",
          "reason" -> "You do not have the necessary permissions to execute this action",
          "details" -> s"You need the have one of the following roles: $roles or one of the following permissions: $permissions"
        ))))
      }
    }
  }
}
