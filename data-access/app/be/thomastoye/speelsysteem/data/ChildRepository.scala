package be.thomastoye.speelsysteem.data

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.models.{ Child, Day, Shift }
import be.thomastoye.speelsysteem.models.Child.Id

import scala.concurrent.Future

trait ChildRepository {

  def findById(id: Id): Future[Option[EntityWithId[Id, Child]]]

  def findAll: Future[Seq[EntityWithId[Id, Child]]]

  def insert(id: Id, child: Child): Future[Id]

  def count: Future[Int]

  def update(id: Id, child: Child): Future[Unit]

  def delete(id: Id): Future[Unit] // TODO Correct? What do we want when id does not exist?
}
