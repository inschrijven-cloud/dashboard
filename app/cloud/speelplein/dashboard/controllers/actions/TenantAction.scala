package cloud.speelplein.dashboard.controllers.actions

import javax.inject.Inject

import cloud.speelplein.models.Tenant
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}

class TenantRequest[A](val tenant: Tenant, request: Request[A])
    extends WrappedRequest[A](request)

class TenantAction @Inject()(val parser: BodyParsers.Default)(
    implicit val executionContext: ExecutionContext)
    extends ActionRefiner[Request, TenantRequest] {
  def refine[A](input: Request[A]): Future[Either[Result, TenantRequest[A]]] =
    Future.successful[Either[Result, TenantRequest[A]]] {
      input.queryString
        .get("tenant")
        .flatMap(_.headOption)
        .map(head =>
          if (Tenant.isValidNewTenantName(head)) Some(Tenant(head)) else None)
        .flatMap {
          case Some(tenant) =>
            Some(new TenantRequest[A](tenant, input))
          case _ => None
        }
        .toRight(BadRequest(
          Json.obj("status" -> "error",
                   "reason" -> "Missing or invalid 'tenant' URL parameter")))
    }
}
