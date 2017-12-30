package cloud.speelplein.dashboard.controllers.actions

import javax.inject.Inject

import cloud.speelplein.dashboard.controllers.api.auth.{Permission, Role}
import cloud.speelplein.data.JwtVerificationService
import cloud.speelplein.models.JsonFormats.auth0AppMetadataFormat
import cloud.speelplein.models.{Auth0AppMetadata, Tenant, TenantUserData}
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class JwtRequest[A](val tenantData: TenantUserData,
                    val tenant: Tenant,
                    val isGlobalSuperUser: Boolean,
                    request: Request[A])
    extends WrappedRequest[A](request)

class JwtVerifyAction @Inject()(
    jwtVerificationService: JwtVerificationService,
    parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends ActionRefiner[TenantRequest, JwtRequest] {

  def refine[A](
      input: TenantRequest[A]): Future[Either[Result, JwtRequest[A]]] =
    Future.successful {
      val t = input.headers
        .get("Authorization")
        .map[Try[String]](Success(_))
        .getOrElse(Failure(
          new Exception("Authorization header not found or Bearer not set")))
        .map(header => header.drop("Bearer ".length))
        .flatMap(token => jwtVerificationService.decode(token))
        .map(json =>
          (Json.parse(json) \ "https://inschrijven.cloud/app_metadata")
            .as[Auth0AppMetadata])
        .map(metadata => {
          val isGlobalSuperUser = metadata.tenants
            .filter(_.name == "global")
            .exists((metadata: TenantUserData) =>
              metadata.roles.contains("superuser"))

          val maybeTenantMetadata =
            metadata.tenants.find(_.name == input.tenant.name)

          (isGlobalSuperUser, maybeTenantMetadata)
        })
        .flatMap {
          case (isGlobalSuperUser, Some(metadata)) =>
            Success(
              new JwtRequest[A](metadata,
                                input.tenant,
                                isGlobalSuperUser,
                                input))
          case _ =>
            Failure(
              new Exception(
                s"Tenant '${input.tenant.name}' not found in JWT token"))
        }

      t match {
        case Success(x) => Right(x)
        case Failure(e) =>
          Left(
            BadRequest(
              Json.obj("status" -> "error",
                       "reason" -> "Missing or invalid JWT",
                       "details" -> e.getMessage)))
      }
    }
}

trait JwtAuthorizationBuilder {
  def authenticate(
      permissions: Seq[Permission]): ActionFunction[TenantRequest, JwtRequest]

  def authenticate(
      permission: Permission): ActionFunction[TenantRequest, JwtRequest] =
    authenticate(Seq(permission))
}

class JwtAuthorizationBuilderImpl @Inject()(
    jwtVerificationService: JwtVerificationService,
    parser: BodyParsers.Default,
    jwtVerifyAction: JwtVerifyAction
)(implicit val ec: ExecutionContext)
    extends JwtAuthorizationBuilder {

  def authenticate(permissions: Seq[Permission])
    : ActionFunction[TenantRequest, JwtRequest] = {
    jwtVerifyAction andThen new ActionRefiner[JwtRequest, JwtRequest] {
      def executionContext: ExecutionContext = ec

      def refine[A](
          request: JwtRequest[A]): Future[Either[Result, JwtRequest[A]]] = {

        val impliedPermissions = request.tenantData.roles
          .flatMap(Role.parseRoleName)
          .flatMap(_.impliedPermissions)

        if (request.tenantData.permissions
              .intersect(permissions.map(_.id))
              .nonEmpty || permissions
              .intersect(impliedPermissions)
              .nonEmpty) {
          Future.successful(Right(request))
        } else {

          implicit val permissionFormat = Json.format[Permission]

          Future.successful(
            Left(Unauthorized(Json.obj(
              "status" -> "error",
              "reason" -> "You do not have the necessary permissions to execute this action",
              "details" -> s"You need the have one of the expected permissions OR have a role that implies this permission",
              "debugInfo" -> Json.obj(
                "tenantName" -> request.tenant.name,
                "needOneOfThesePermissions" -> permissions,
                "yourRoles" -> request.tenantData.roles,
                "yourPermissions" -> request.tenantData.permissions,
                "yourImpliedPermissionsByRole" -> impliedPermissions
              )
            ))))
        }
      }
    }
  }
}
