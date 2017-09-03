package helpers

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.data.ChildRepository
import be.thomastoye.speelsysteem.models.Child
import be.thomastoye.speelsysteem.models.Child.Id
import be.thomastoye.speelsysteem.models.Day.Id
import be.thomastoye.speelsysteem.models.Shift.Id
import com.google.inject.AbstractModule

import scala.concurrent.Future

class StubChildRepository extends ChildRepository {
  override def findById(id: Id): Future[Option[EntityWithId[Id, Child]]] = ???

  override def findAll: Future[Seq[EntityWithId[Child.Id, Child]]] = Future.successful(Seq.empty)

  override def insert(id: Id, child: Child): Future[Id] = ???

  override def count: Future[Int] = ???

  override def update(id: Id, child: Child): Future[Unit] = ???

  override def delete(id: Id): Future[Unit] = ???

  override def setMerged(retiredId: Id, absorpedIntoId: Id): Future[Unit] = ???
}

class StubChildRepositoryModule extends AbstractModule {
  override def configure() = {
    bind(classOf[ChildRepository]).to(classOf[StubChildRepository])
  }
}
