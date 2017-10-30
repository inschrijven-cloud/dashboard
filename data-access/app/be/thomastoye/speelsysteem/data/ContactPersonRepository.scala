package be.thomastoye.speelsysteem.data

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.models.{ ContactPerson, Tenant }
import be.thomastoye.speelsysteem.models.ContactPerson.Id

import scala.concurrent.Future

trait ContactPersonRepository {

  def findById(id: Id)(implicit tenant: Tenant): Future[Option[EntityWithId[Id, ContactPerson]]]

  def findAll(implicit tenant: Tenant): Future[Seq[EntityWithId[Id, ContactPerson]]]

  def insert(id: Id, child: ContactPerson)(implicit tenant: Tenant): Future[Id]

  def count(implicit tenant: Tenant): Future[Int]

  def update(id: Id, child: ContactPerson)(implicit tenant: Tenant): Future[Unit]

  def delete(id: Id)(implicit tenant: Tenant): Future[Unit] // TODO Correct? What do we want when id does not exist?

}
