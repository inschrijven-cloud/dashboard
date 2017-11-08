package helpers

import cloud.speelplein.models.Child
import cloud.speelplein.models.Shift.Id
import cloud.speelplein.data.ChildRepository
import cloud.speelplein.models.Tenant
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
