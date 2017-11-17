package cloud.speelplein.data.auth0

import javax.inject.Inject

import cloud.speelplein.data.{JwtVerificationService, UserService}
import cloud.speelplein.models.{Tenant, TenantUserData, User}
import cloud.speelplein.models.JsonFormats.{
  auth0UserReads,
  tenantUserDataWrites
}
import com.typesafe.scalalogging.StrictLogging
import play.api.Configuration
import play.api.cache._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

trait Auth0Configuration {
  val clientId: String
  val clientSecret: String
  val audience: String
}

class Auth0ConfigurationFromConfigFile @Inject()(config: Configuration)
    extends Auth0Configuration {
  override val clientId = config.get[String]("auth0.clientId")
  override val clientSecret = config.get[String]("auth0.clientSecret")
  override val audience = config.get[String]("auth0.audience")
}

class Auth0UserService @Inject()(cache: AsyncCacheApi,
                                 wsClient: WSClient,
                                 jwtVerificationService: JwtVerificationService,
                                 auth0Configuration: Auth0Configuration,
                                 implicit val ec: ExecutionContext)
    extends UserService
    with StrictLogging {

  override def getAll: Future[Seq[User]] =
    getTokenFromCacheOrUpdateIfNecessary flatMap { token =>
      // TODO check if there are other pages (if total > 100)
      // TODO should probably cache result
      wsClient
        .url("https://inschrijven-cloud.eu.auth0.com/api/v2/users?per_page=100&page=0&include_totals=true&sort=created_at%3A1")
        .withHttpHeaders("Authorization" -> s"Bearer $token")
        .get()
        .map { wsResponse =>
          (wsResponse.json \ "users").as[Seq[User]]
        }
    }

  override def getById(id: String): Future[Option[User]] =
    getTokenFromCacheOrUpdateIfNecessary flatMap { token =>
      wsClient
        .url("https://inschrijven-cloud.eu.auth0.com/api/v2/users/" + id)
        .withHttpHeaders("Authorization" -> s"Bearer $token")
        .get()
        .map { wsResponse =>
          wsResponse.status match {
            case 200 => Some(wsResponse.json.as[User])
            case 404 => None
            case _   => throw new Exception(wsResponse.body)
          }
        }
    }

  override def setTenantDataForUser(
      userId: String,
      tenantData: Seq[TenantUserData]): Future[Unit] = ???

  override def setRolesAndPermissionsForUser(
      userId: String,
      tenant: Tenant,
      roles: Seq[String],
      permissions: Seq[String]): Future[Option[Unit]] =
    getTokenFromCacheOrUpdateIfNecessary flatMap { token =>
      getById(userId) flatMap {
        case Some(user) => {
          val newMetadata = user.tenants.filter(_.name != tenant.name) :+ TenantUserData(
            tenant.name,
            roles,
            permissions)

          wsClient
            .url(
              "https://inschrijven-cloud.eu.auth0.com/api/v2/users/" + userId)
            .withHttpHeaders("Authorization" -> s"Bearer $token")
            .patch(Json.obj("app_metadata" -> Json.obj(
              "tenants" -> Json.toJson(newMetadata))))
            .map(wsResponse =>
              wsResponse.status match {
                case 200 => Some(())
                case _ =>
                  throw new Exception(
                    s"Error while contacting Auth0: ${wsResponse.body}")
            })
        }
        case _ => Future.successful(None)
      }
    }

  private def getTokenFromCacheOrUpdateIfNecessary: Future[String] = {
    cache.get[String]("auth0.accessToken") flatMap { maybeToken =>
      maybeToken filter (jwtVerificationService.isValid) map (Future
        .successful(_)) getOrElse {
        getTokenFromAuth0 flatMap { newToken =>
          cache.set("auth0.accessToken", newToken, 1.hour) map (_ => newToken)
        }
      }
    }
  }

  private def getTokenFromAuth0: Future[String] = {
    logger.info("Requesting new token from Auth0 for user management")

    wsClient
      .url("https://inschrijven-cloud.eu.auth0.com/oauth/token")
      .withHttpHeaders("Content-Type" -> "application/json")
      .post[JsObject](Json.obj(
        "client_id" -> auth0Configuration.clientId,
        "client_secret" -> auth0Configuration.clientSecret,
        "audience" -> auth0Configuration.audience,
        "grant_type" -> "client_credentials"
      )) map (wsRes => (wsRes.json \ "access_token").as[String])
  }
}
