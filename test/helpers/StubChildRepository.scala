package helpers

import be.thomastoye.speelsysteem.data.ChildRepository
import be.thomastoye.speelsysteem.models.Child
import be.thomastoye.speelsysteem.models.Child.Id
import be.thomastoye.speelsysteem.models.Day.Id
import be.thomastoye.speelsysteem.models.Shift.Id
import com.google.inject.AbstractModule

import scala.concurrent.Future

class StubChildRepository extends ChildRepository {
  override def findById(id: Id): Future[Option[(Id, Child)]] = ???

  override def findAll: Future[Seq[(Id, Child)]] = Future.successful(Seq.empty)

  override def insert(id: Id, child: Child): Future[Id] = ???

  override def count: Future[Int] = ???

  override def update(id: Id, child: Child): Future[Unit] = ???

  override def addAttendancesForChild(id: Id, dayId: Id, shifts: Seq[Id]): Future[Option[Unit]] = ???
}

class StubChildRepositoryModule extends AbstractModule {
  override def configure() = {
    bind(classOf[ChildRepository]).to(classOf[StubChildRepository])
  }
}
