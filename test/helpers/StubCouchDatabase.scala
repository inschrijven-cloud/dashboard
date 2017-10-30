package helpers

import be.thomastoye.speelsysteem.data.couchdb.CouchDatabase
import be.thomastoye.speelsysteem.models.Tenant
import com.google.inject.AbstractModule
import com.ibm.couchdb.TypeMapping

class StubCouchDatabase extends CouchDatabase {
  override def getDb(typeMapping: TypeMapping, tenant: Tenant) = null
  override def getDb(typeMapping: TypeMapping, dbName: String) = null
}

class StubCouchDatabaseModule extends AbstractModule {
  override def configure() = {
    bind(classOf[CouchDatabase]).to(classOf[StubCouchDatabase])
  }
}
