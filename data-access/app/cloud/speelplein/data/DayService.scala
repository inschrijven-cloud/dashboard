package cloud.speelplein.data

import cloud.speelplein.EntityWithId
import cloud.speelplein.models.Day.Id
import cloud.speelplein.models.Tenant
import cloud.speelplein.models.{Day, Tenant}

import scala.concurrent.Future

trait DayService {

  def findAll(implicit tenant: Tenant): Future[Seq[EntityWithId[Id, Day]]]

  def insert(day: Day)(implicit tenant: Tenant): Future[Unit]

  def findById(id: Day.Id)(
      implicit tenant: Tenant): Future[Option[EntityWithId[Id, Day]]]

  def update(id: Day.Id, day: Day)(implicit tenant: Tenant): Future[Unit]
}
