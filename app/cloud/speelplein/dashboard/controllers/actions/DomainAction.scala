package cloud.speelplein.dashboard.controllers.actions

import javax.inject.Inject

import cloud.speelplein.models.Tenant
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}

class DomainRequest[A](val userDomain: String,
                       val tenant: Tenant,
                       request: Request[A])
    extends WrappedRequest[A](request)

class DomainAction @Inject()(val parser: BodyParsers.Default)(
    implicit val executionContext: ExecutionContext)
    extends ActionRefiner[Request, DomainRequest] {
  def refine[A](input: Request[A]): Future[Either[Result, DomainRequest[A]]] =
    Future.successful[Either[Result, DomainRequest[A]]] {
      input.queryString
        .get("domain")
        .flatMap(_.headOption)
        .map(head => (head, Tenant.fromDomain(head)))
        .flatMap {
          case (userDomain, Some(tenant)) =>
            Some(new DomainRequest[A](userDomain, tenant, input))
          case _ => None
        }
        .toRight(
          BadRequest(Json.obj("status" -> "error",
                              "reason" -> "Missing 'domain' URL parameter")))
    }
}
