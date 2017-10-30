package helpers

import be.thomastoye.speelsysteem.data.ChildRepository
import be.thomastoye.speelsysteem.models.{ Child, Tenant }
import be.thomastoye.speelsysteem.models.Shift.Id
import com.google.inject.AbstractModule

class StubChildRepository extends ChildRepository {
  override def findById(id: Id)(implicit tenant: Tenant) = ???

  override def findAll(implicit tenant: Tenant) = ???

  override def insert(id: Id, child: Child)(implicit tenant: Tenant) = ???

  override def count(implicit tenant: Tenant) = ???

  override def update(id: Id, child: Child)(implicit tenant: Tenant) = ???

  override def delete(id: Id)(implicit tenant: Tenant) = ???

  override def setMerged(retiredId: Id, absorpedIntoId: Id)(implicit tenant: Tenant) = ???
}

class StubChildRepositoryModule extends AbstractModule {
  override def configure() = {
    bind(classOf[ChildRepository]).to(classOf[StubChildRepository])
  }
}
