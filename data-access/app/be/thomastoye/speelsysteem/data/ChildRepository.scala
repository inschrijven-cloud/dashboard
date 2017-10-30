package be.thomastoye.speelsysteem.data

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.models.{ Child, Tenant }
import be.thomastoye.speelsysteem.models.Child.Id

import scala.concurrent.Future

trait ChildRepository {

  def findById(id: Id)(implicit tenant: Tenant): Future[Option[EntityWithId[Id, Child]]]

  def findAll(implicit tenant: Tenant): Future[Seq[EntityWithId[Id, Child]]]

  def insert(id: Id, child: Child)(implicit tenant: Tenant): Future[Id]

  def count(implicit tenant: Tenant): Future[Int]

  def update(id: Id, child: Child)(implicit tenant: Tenant): Future[Unit]

  def delete(id: Id)(implicit tenant: Tenant): Future[Unit] // TODO Correct? What do we want when id does not exist?

  def setMerged(retiredId: Id, absorpedIntoId: Id)(implicit tenant: Tenant): Future[Unit]
}
