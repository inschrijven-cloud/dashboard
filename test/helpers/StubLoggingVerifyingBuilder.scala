package helpers

import cloud.speelplein.dashboard.controllers.actions._
import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.models.{AuditLogData, TenantUserData}
import com.google.inject.AbstractModule
import play.api.mvc.{ActionFunction, Result}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.ExecutionContext.Implicits._

/** Doesn't perform authorization or logging, lets every request pass */
class StubLoggingVerifyingBuilder extends LoggingVerifyingBuilder {
  override def logAndVerify(permission: Permission, data: AuditLogData) =
    new ActionFunction[TenantRequest, AuditLoggingRequest] {
      override def invokeBlock[A](
          request: TenantRequest[A],
          block: AuditLoggingRequest[A] => Future[Result]) = {
        val jwtRequest = new JwtRequest[A](
          TenantUserData(request.tenant.name, Seq.empty, Seq.empty),
          request.tenant,
          false,
          "raw token stub",
          request)

        block(
          new AuditLoggingRequest[A](jwtRequest.tenantData,
                                     jwtRequest.tenant,
                                     jwtRequest.isGlobalSuperUser,
                                     jwtRequest.rawJwt,
                                     jwtRequest))
      }

      override protected def executionContext: ExecutionContextExecutor = global
    }
}

class StubLoggingVerifyingBuilderModule extends AbstractModule {
  override def configure() = {
    bind(classOf[LoggingVerifyingBuilder])
      .to(classOf[StubLoggingVerifyingBuilder])
  }
}
