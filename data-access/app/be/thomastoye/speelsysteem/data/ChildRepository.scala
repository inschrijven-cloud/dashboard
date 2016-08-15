package be.thomastoye.speelsysteem.data

import be.thomastoye.speelsysteem.models.{Child, Day, Shift}
import be.thomastoye.speelsysteem.models.Child.Id

import scala.concurrent.Future

trait ChildRepository {

  def findById(id: Id): Future[Option[(Id, Child)]]

  def findAll: Future[Seq[(Id, Child)]]

  def insert(id: Id, child: Child): Future[Id]

  def count: Future[Int]

  def update(id: Id, child: Child): Future[Unit]

  def addAttendancesForChild(id: Id, dayId: Day.Id, shifts: Seq[Shift.Id]): Future[Option[Unit]] // TODO should be Try
}
