package cloud.speelplein.data

import cloud.speelplein.models.{Tenant, TenantUserData, User}

import scala.concurrent.Future

trait UserService {
  def getAll: Future[Seq[User]]
  def getById(id: String): Future[Option[User]]
  def setTenantDataForUser(userId: String,
                           tenantData: Seq[TenantUserData]): Future[Unit]
  def setRolesAndPermissionsForUser(
      userId: String,
      tenant: Tenant,
      roles: Seq[String],
      permissions: Seq[String]): Future[Option[Unit]]
}
