package be.thomastoye.speelsysteem.data

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.models.Day.Id
import be.thomastoye.speelsysteem.models.Day

import scala.concurrent.Future

trait DayService {

  def findAll: Future[Seq[EntityWithId[Id, Day]]]

  def insert(day: Day): Future[Unit]

  def findById(id: Day.Id): Future[Option[Day]]

  def update(id: Day.Id, day: Day): Future[Unit]
}
