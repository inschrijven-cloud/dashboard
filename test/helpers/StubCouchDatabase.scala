package helpers

import be.thomastoye.speelsysteem.data.couchdb.CouchDatabase
import com.google.inject.AbstractModule
import com.ibm.couchdb.{CouchDbApi, TypeMapping}

class StubCouchDatabase extends CouchDatabase {
  override val db: CouchDbApi = null
  override def getDb(suffix: String, typeMapping: TypeMapping): CouchDbApi = null
}

class StubCouchDatabaseModule extends AbstractModule {
  override def configure() = {
    bind(classOf[CouchDatabase]).to(classOf[StubCouchDatabase])
  }
}
