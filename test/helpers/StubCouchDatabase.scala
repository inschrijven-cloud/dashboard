package helpers

import be.thomastoye.speelsysteem.data.couchdb.CouchDatabase
import com.google.inject.AbstractModule
import com.ibm.couchdb.CouchDbApi

class StubCouchDatabase extends CouchDatabase {
  override val db: CouchDbApi = null
}

class StubCouchDatabaseModule extends AbstractModule {
  override def configure() = {
    bind(classOf[CouchDatabase]).to(classOf[StubCouchDatabase])
  }
}
