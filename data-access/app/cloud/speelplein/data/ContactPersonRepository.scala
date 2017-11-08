package cloud.speelplein.data

import cloud.speelplein.EntityWithId
import cloud.speelplein.models.ContactPerson.Id
import cloud.speelplein.models.{ContactPerson, Tenant}

import scala.concurrent.Future

trait ContactPersonRepository {

  def findById(id: Id)(
      implicit tenant: Tenant): Future[Option[EntityWithId[Id, ContactPerson]]]

  def findAll(
      implicit tenant: Tenant): Future[Seq[EntityWithId[Id, ContactPerson]]]

  def insert(id: Id, child: ContactPerson)(implicit tenant: Tenant): Future[Id]

  def count(implicit tenant: Tenant): Future[Int]

  def update(id: Id, child: ContactPerson)(
      implicit tenant: Tenant): Future[Unit]

  def delete(id: Id)(implicit tenant: Tenant): Future[Unit] // TODO Correct? What do we want when id does not exist?

}
