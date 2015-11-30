package authorization

import com.mohiva.play.silhouette.api.Authorization
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models.tenant.AuthCrewUser
import play.api.i18n.Messages
import play.api.mvc.Request
import models.Role

import scala.concurrent.Future

/**
  * Authorization implementation that restricts endpoints to users with a specific role
  */
case class WithRole(role: Role) extends Authorization[AuthCrewUser, JWTAuthenticator] {
  def isAuthorized[B](user: AuthCrewUser, authenticator: JWTAuthenticator)(implicit request: Request[B],
                       messages: Messages): Future[Boolean] =
  {
    Future.successful(user.roles.contains(role))
  }
}