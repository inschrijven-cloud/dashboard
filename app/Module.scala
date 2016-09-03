import be.thomastoye.speelsysteem.data.couchdb.{CouchChildRepository, CouchCrewRepository, CouchDatabase, CouchDatabaseImpl}
import be.thomastoye.speelsysteem.data.util.{UuidService, UuidServiceImpl}
import be.thomastoye.speelsysteem.data.{ChildRepository, CrewRepository}
import com.google.inject.AbstractModule

class Module extends AbstractModule {
  override def configure() = {
    bind(classOf[ChildRepository]).to(classOf[CouchChildRepository])
    bind(classOf[CrewRepository]).to(classOf[CouchCrewRepository])
    bind(classOf[UuidService]).to(classOf[UuidServiceImpl])
    bind(classOf[CouchDatabase]).to(classOf[CouchDatabaseImpl])
  }
}
