package be.thomastoye.speelsysteem.data

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.models.{ Crew, Tenant }

import scala.concurrent.Future

trait CrewRepository {

  def findById(id: Crew.Id)(implicit tenant: Tenant): Future[Option[EntityWithId[Crew.Id, Crew]]]

  def findAll(implicit tenant: Tenant): Future[Seq[EntityWithId[Crew.Id, Crew]]]

  def insert(id: Crew.Id, crewMember: Crew)(implicit tenant: Tenant): Future[Crew.Id]

  def count(implicit tenant: Tenant): Future[Int]

  def update(id: Crew.Id, crewMember: Crew)(implicit tenant: Tenant): Future[Unit]

  def delete(id: Crew.Id)(implicit tenant: Tenant): Future[Unit] // TODO Correct? What do we want when id does not exist?
}
