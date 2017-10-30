package be.thomastoye.speelsysteem.dashboard.controllers.actions

import javax.inject.Inject

import be.thomastoye.speelsysteem.models.Tenant
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.{ ExecutionContext, Future }

class DomainRequest[A](val userDomain: String, val tenant: Tenant, request: Request[A]) extends WrappedRequest[A](request)

class DomainAction @Inject() (val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext) extends ActionRefiner[Request, DomainRequest] {
  def refine[A](input: Request[A]): Future[Either[Result, DomainRequest[A]]] = Future.successful {
    input.queryString.get("domain")
      .map(x => (x.head, Tenant.fromDomain(x.head)))
      .filter(tuple => tuple._2.isDefined)
      .map(domain => new DomainRequest[A](domain._1, domain._2.get, input))
      .toRight(BadRequest(Json.obj("status" -> "error", "reason" -> "Missing 'domain' URL parameter")))
  }
}
