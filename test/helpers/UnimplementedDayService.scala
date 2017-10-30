package helpers

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.data.DayService
import be.thomastoye.speelsysteem.models.{ Day, Tenant }
import be.thomastoye.speelsysteem.models.Day.Id

import scala.concurrent.Future

trait UnimplementedDayService extends DayService {
  override def findAll(implicit tenant: Tenant): Future[Seq[EntityWithId[Id, Day]]] = ???
  override def insert(day: Day)(implicit tenant: Tenant) = ???
  override def findById(id: Id)(implicit tenant: Tenant) = ???
  override def update(id: Id, day: Day)(implicit tenant: Tenant) = ???
}
