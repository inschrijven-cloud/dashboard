package helpers

import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.dashboard.controllers.actions.{
  JwtAuthorizationBuilder,
  JwtRequest,
  TenantRequest
}
import cloud.speelplein.models.TenantUserData
import play.api.mvc.{ActionFunction, Result}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.ExecutionContext.Implicits._

/** Doesn't perform authorization, lets every request pass */
class StubJwtAuthorizationBuilder extends JwtAuthorizationBuilder {
  override def authenticate(permissions: Seq[Permission]) =
    new ActionFunction[TenantRequest, JwtRequest] {
      override def invokeBlock[A](request: TenantRequest[A],
                                  block: JwtRequest[A] => Future[Result]) = {
        block(
          new JwtRequest(
            TenantUserData(request.tenant.name, Seq.empty, Seq.empty),
            request.tenant,
            false,
            "raw token stub",
            request))
      }

      override protected def executionContext: ExecutionContextExecutor = global
    }
}
