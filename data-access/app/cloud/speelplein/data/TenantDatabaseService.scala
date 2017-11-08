package cloud.speelplein.data

import cloud.speelplein.data.couchdb.CouchDbConfig
import cloud.speelplein.data.couchdb.CouchDbConfig
import cloud.speelplein.models.DbName
import com.ibm.couchdb.{ CouchDesign, Res }
import com.ibm.couchdb.Res.DocOk
import play.api.libs.json.JsValue

import scala.concurrent.Future

trait TenantDatabaseService {
  def all: Future[Seq[DbName]]

  def create(db: DbName): Future[Res.Ok]

  def details(db: DbName): Future[Res.DbInfo]

  def drop(db: DbName): Future[Res.Ok]

  /**
   * Check if a design doc exists
   * @return Maybe the rev of the existing design doc
   */
  def designDocExists(db: DbName, designName: String): Future[Option[String]]

  def createDesignDoc(db: DbName, couchDesign: CouchDesign): Future[DocOk]

  def startReplicationToRemote(db: DbName, target: CouchDbConfig): Future[JsValue]

  def startReplicationFromRemote(db: CouchDbConfig, target: DbName): Future[JsValue]
}
