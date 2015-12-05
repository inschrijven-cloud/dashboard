package controllers.api

import java.util.UUID

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.impl.authenticators.{JWTAuthenticator, CookieAuthenticator}
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import dao.RepoFor
import models.tenant.persistable.PersistableContactPerson
import models.tenant.{ContactPerson, AuthCrewUser, Child}
import play.api._
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.MessagesApi
import play.api.libs.json._

class ContactPersonController @Inject()(dbConfigProvider: DatabaseConfigProvider,
  messagesApi: MessagesApi,
  env: Environment[AuthCrewUser, JWTAuthenticator],
  socialProviderRegistry: SocialProviderRegistry)
  extends GenericSecureApiController(dbConfigProvider, messagesApi, env, socialProviderRegistry)
{
  override type Id = UUID
  override type Model = ContactPerson
  override type PersistedModel = PersistableContactPerson

  override val repo: RepoFor[PersistedModel, Id] = dao.tenant.ContactPersonRepo

  override def convertToDisplayable: PersistedModel => Model = _.convert
  override def convertToPersistable: Model => PersistedModel = PersistableContactPerson.build

  override implicit val reads: Reads[Model] = models.tenant.json.ContactPersonJson.contactPersonReads
  override implicit val writes: Writes[Model] = models.tenant.json.ContactPersonJson.contactPersonWrites

}