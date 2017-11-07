package be.thomastoye.speelsysteem.dashboard.controllers.actions

import javax.inject.Inject

import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.{ ExecutionContext, Future }

class GlobalDomainOnlyAction @Inject() (val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext)
    extends ActionRefiner[DomainRequest, DomainRequest] {
  def refine[A](input: DomainRequest[A]): Future[Either[Result, DomainRequest[A]]] = Future.successful {
    if (input.tenant.name == "global") {
      Right(input)
    } else {
      Left(BadRequest(Json.obj("status" -> "error", "reason" -> "This route is only availaible on the tenant 'global'.")))
    }
  }
}
