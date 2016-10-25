package helpers

import be.thomastoye.speelsysteem.data.DayService
import be.thomastoye.speelsysteem.models.Child.Id
import be.thomastoye.speelsysteem.models.Day
import be.thomastoye.speelsysteem.models.Day.Id

import scala.concurrent.Future

trait UnimplementedDayService extends DayService {
  override def findAll: Future[Seq[(Id, Day)]] = ???
  override def findAttendancesForChild(id: Id): Future[Seq[Day]] = ???
  override def insert(day: Day): Future[Unit] = ???
  override def findById(id: Id): Future[Option[Day]] = ???
  override def update(id: Id, day: Day): Future[Unit] = ???
}
