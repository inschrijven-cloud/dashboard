package helpers

import be.thomastoye.speelsysteem.dashboard.controllers.actions.{ DomainRequest, JwtAuthorizationBuilder, JwtRequest }
import be.thomastoye.speelsysteem.models.TenantMetadata
import play.api.mvc.{ ActionFunction, Result }

import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.concurrent.ExecutionContext.Implicits._

/** Doesn't perform authorization, lets every request pass */
class StubJwtAuthorizationBuilder extends JwtAuthorizationBuilder {
  override def authenticate(permissions: Seq[String], roles: Seq[String]) = new ActionFunction[DomainRequest, JwtRequest] {
    override def invokeBlock[A](request: DomainRequest[A], block: JwtRequest[A] => Future[Result]) = {
      block(new JwtRequest(TenantMetadata(request.tenant.name, Seq.empty, Seq.empty), request.userDomain, request.tenant, request))
    }

    override protected def executionContext: ExecutionContextExecutor = global
  }
}
