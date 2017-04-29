package be.thomastoye.speelsysteem.data

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.models.ContactPerson
import be.thomastoye.speelsysteem.models.ContactPerson.Id

import scala.concurrent.Future

trait ContactPersonRepository {

  def findById(id: Id): Future[Option[EntityWithId[Id, ContactPerson]]]

  def findAll: Future[Seq[EntityWithId[Id, ContactPerson]]]

  def insert(id: Id, child: ContactPerson): Future[Id]

  def count: Future[Int]

  def update(id: Id, child: ContactPerson): Future[Unit]

  def delete(id: Id): Future[Unit] // TODO Correct? What do we want when id does not exist?

}
