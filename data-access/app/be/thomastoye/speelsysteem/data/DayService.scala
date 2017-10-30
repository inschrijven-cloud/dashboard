package be.thomastoye.speelsysteem.data

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.models.Day.Id
import be.thomastoye.speelsysteem.models.{ Day, Tenant }

import scala.concurrent.Future

trait DayService {

  def findAll(implicit tenant: Tenant): Future[Seq[EntityWithId[Id, Day]]]

  def insert(day: Day)(implicit tenant: Tenant): Future[Unit]

  def findById(id: Day.Id)(implicit tenant: Tenant): Future[Option[EntityWithId[Id, Day]]]

  def update(id: Day.Id, day: Day)(implicit tenant: Tenant): Future[Unit]
}
