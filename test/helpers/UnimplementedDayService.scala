package helpers

import cloud.speelplein.EntityWithId
import cloud.speelplein.models.Tenant
import cloud.speelplein.models.Day.Id
import cloud.speelplein.data.DayService
import cloud.speelplein.models.{ Day, Tenant }

import scala.concurrent.Future

trait UnimplementedDayService extends DayService {
  override def findAll(implicit tenant: Tenant): Future[Seq[EntityWithId[Id, Day]]] = ???
  override def insert(day: Day)(implicit tenant: Tenant) = ???
  override def findById(id: Id)(implicit tenant: Tenant) = ???
  override def update(id: Id, day: Day)(implicit tenant: Tenant) = ???
}
