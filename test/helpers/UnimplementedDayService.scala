package helpers

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.data.DayService
import be.thomastoye.speelsysteem.models.Child.Id
import be.thomastoye.speelsysteem.models.Day
import be.thomastoye.speelsysteem.models.Day.Id

import scala.concurrent.Future

trait UnimplementedDayService extends DayService {
  override def findAll: Future[Seq[EntityWithId[Day.Id, Day]]] = ???
  override def insert(day: Day): Future[Unit] = ???
  override def findById(id: Id): Future[Option[EntityWithId[Id, Day]]] = ???
  override def update(id: Id, day: Day): Future[Unit] = ???
}
