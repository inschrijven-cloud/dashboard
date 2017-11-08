package cloud.speelplein.data

import cloud.speelplein.data.couchdb.CouchDbConfig
import cloud.speelplein.data.couchdb.CouchDbConfig
import cloud.speelplein.models.Tenant
import com.ibm.couchdb.Res
import play.api.libs.json.{ JsObject, JsValue }

import scala.concurrent.Future

trait TenantsService {
  def all: Future[Seq[Tenant]]
  def create(tenant: Tenant): Future[Res.Ok]
  def details(tenant: Tenant): Future[Unit] // TODO does nothing yet
  def initializeDatabase(tenant: Tenant): Future[JsObject]
  def syncTo(tenant: Tenant, remote: CouchDbConfig): Future[JsValue]
  def syncFrom(tenant: Tenant, remote: CouchDbConfig): Future[JsValue]
}
